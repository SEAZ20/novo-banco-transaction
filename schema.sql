-- =============================================================================
-- NovoBanco Transaction Service — Esquema de Base de Datos
-- Motor: Oracle Database 21c XE
-- Versión OJDBC: ojdbc11 23.4.0.24.05
-- =============================================================================
--
-- JUSTIFICACIÓN DEL MOTOR (Oracle 21c XE):
--   1. ACID nativo + soporte de transacciones distribuidas con XA — garantiza
--      la atomicidad de transferencias entre cuentas sin coordinación externa.
--   2. SELECT ... FOR UPDATE con NOWAIT/SKIP LOCKED — control de concurrencia
--      optimista/pesimista sin bloqueos silenciosos; crítico para evitar saldo
--      negativo bajo escrituras simultáneas.
--   3. CHECK CONSTRAINTS evaluados en cada DML — el saldo >= 0 se refuerza
--      a nivel de motor, no solo en la capa de aplicación.
--   4. IDENTITY columns (desde 12c) y SYSTIMESTAMP — simplifica el DDL y
--      garantiza timestamps con precisión de nanosegundos.
--   5. Particionado por rango (futuro): cuando TRANSACTIONS supere 100M filas
--      se puede activar particionado por CREATED_AT sin cambiar el esquema lógico.
--
-- DECISIONES DE DISEÑO:
--   - Normalización completa (3FN): CUSTOMERS → ACCOUNTS → TRANSACTIONS.
--   - balance_after en cada transacción: permite reconstruir el historial de
--     saldo sin reagrupar todas las transacciones previas (O(1) vs O(n)).
--   - related_account_id en TRANSACTIONS: vincula las dos patas de una
--     transferencia (TRANSFER_DEBIT ↔ TRANSFER_CREDIT) sin tabla pivot extra.
--   - Índice compuesto (account_id, created_at DESC): responde O(log n) la
--     consulta de historial paginado, el caso de uso más frecuente en producción.
--   - Índice compuesto (account_id, type, created_at): soporta el conteo de
--     transferencias salientes de un cliente en un período.
--
-- CONSULTAS CUBIERTAS (sección 4.1):
--   Q1 — Saldo actual:
--        SELECT balance FROM accounts WHERE account_number = :num;
--        → idx_accounts_account_number (UNIQUE)
--   Q2 — Últimos 20 movimientos:
--        SELECT * FROM transactions WHERE account_id = :id
--        ORDER BY created_at DESC FETCH FIRST 20 ROWS ONLY;
--        → idx_transactions_account_created_at
--   Q3 — Transferencias salientes en 30 días:
--        SELECT COUNT(*) FROM transactions
--        WHERE account_id = :id AND type = 'TRANSFER_DEBIT'
--          AND created_at >= SYSTIMESTAMP - INTERVAL '30' DAY;
--        → idx_transactions_account_type_date
--   Q4 — Referencia única (detección de duplicados):
--        SELECT 1 FROM transactions WHERE reference = :ref;
--        → uk_transactions_reference (UNIQUE)
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. CUSTOMERS — Clientes del banco
-- -----------------------------------------------------------------------------
CREATE TABLE customers (
    id              NUMBER          GENERATED ALWAYS AS IDENTITY,
    name            VARCHAR2(200 CHAR) NOT NULL,
    email           VARCHAR2(150 CHAR) NOT NULL,
    document_number VARCHAR2(50 CHAR)  NOT NULL,
    created_at      TIMESTAMP       DEFAULT SYSTIMESTAMP NOT NULL,

    CONSTRAINT pk_customers               PRIMARY KEY (id),
    CONSTRAINT uk_customers_email         UNIQUE (email),
    CONSTRAINT uk_customers_document_number UNIQUE (document_number)
);

COMMENT ON TABLE  customers                  IS 'Clientes del banco';
COMMENT ON COLUMN customers.document_number  IS 'DNI / RUC / pasaporte';

-- -----------------------------------------------------------------------------
-- 2. ACCOUNTS — Cuentas bancarias (ahorros / corriente)
-- -----------------------------------------------------------------------------
CREATE TABLE accounts (
    id             NUMBER          GENERATED ALWAYS AS IDENTITY,
    account_number VARCHAR2(20 CHAR)  NOT NULL,
    customer_id    NUMBER             NOT NULL,
    type           VARCHAR2(20 CHAR)  NOT NULL,
    currency       VARCHAR2(3 CHAR)   DEFAULT 'USD'    NOT NULL,
    balance        NUMBER(19, 4)      DEFAULT 0         NOT NULL,
    status         VARCHAR2(20 CHAR)  DEFAULT 'ACTIVE'  NOT NULL,
    created_at     TIMESTAMP          DEFAULT SYSTIMESTAMP NOT NULL,
    updated_at     TIMESTAMP          DEFAULT SYSTIMESTAMP NOT NULL,

    CONSTRAINT pk_accounts                PRIMARY KEY (id),
    CONSTRAINT uk_accounts_account_number UNIQUE (account_number),
    CONSTRAINT fk_accounts_customer_id    FOREIGN KEY (customer_id)
                                          REFERENCES customers (id),
    -- Regla de negocio crítica: saldo nunca negativo, reforzado en BD
    CONSTRAINT chk_accounts_balance       CHECK (balance >= 0),
    CONSTRAINT chk_accounts_type          CHECK (type     IN ('SAVINGS', 'CHECKING')),
    CONSTRAINT chk_accounts_status        CHECK (status   IN ('ACTIVE', 'BLOCKED', 'CLOSED')),
    CONSTRAINT chk_accounts_currency      CHECK (currency = 'USD')
);

COMMENT ON TABLE  accounts         IS 'Cuentas bancarias (ahorros o corriente)';
COMMENT ON COLUMN accounts.balance IS 'Saldo disponible; nunca puede ser negativo (CHK + validación de aplicación)';
COMMENT ON COLUMN accounts.status  IS 'ACTIVE = opera normal | BLOCKED = suspendida | CLOSED = baja definitiva';

-- Índices de ACCOUNTS
CREATE INDEX idx_accounts_customer_id ON accounts (customer_id);
CREATE INDEX idx_accounts_status       ON accounts (status);

-- -----------------------------------------------------------------------------
-- 3. TRANSACTIONS — Registro inmutable de cada movimiento financiero
-- -----------------------------------------------------------------------------
CREATE TABLE transactions (
    id                 NUMBER          GENERATED ALWAYS AS IDENTITY,
    reference          VARCHAR2(50 CHAR)  NOT NULL,
    account_id         NUMBER             NOT NULL,
    related_account_id NUMBER,                          -- segunda pata de transferencia
    type               VARCHAR2(30 CHAR)  NOT NULL,
    amount             NUMBER(19, 4)      NOT NULL,
    balance_after      NUMBER(19, 4)      NOT NULL,     -- saldo de la cuenta tras el movimiento
    status             VARCHAR2(20 CHAR)  NOT NULL,
    description        VARCHAR2(500 CHAR),
    created_at         TIMESTAMP          DEFAULT SYSTIMESTAMP NOT NULL,

    CONSTRAINT pk_transactions                  PRIMARY KEY (id),
    CONSTRAINT uk_transactions_reference        UNIQUE (reference),
    CONSTRAINT fk_transactions_account_id       FOREIGN KEY (account_id)
                                                REFERENCES accounts (id),
    CONSTRAINT fk_transactions_related_account  FOREIGN KEY (related_account_id)
                                                REFERENCES accounts (id),
    CONSTRAINT chk_transactions_amount          CHECK (amount > 0),
    CONSTRAINT chk_transactions_balance_after   CHECK (balance_after >= 0),
    CONSTRAINT chk_transactions_type            CHECK (type IN (
                                                    'DEPOSIT',
                                                    'WITHDRAWAL',
                                                    'TRANSFER_DEBIT',
                                                    'TRANSFER_CREDIT'
                                                )),
    CONSTRAINT chk_transactions_status          CHECK (status IN (
                                                    'SUCCESS',
                                                    'FAILED',
                                                    'REVERSED'
                                                ))
);

COMMENT ON TABLE  transactions                    IS 'Registro inmutable de movimientos. Nunca se eliminan ni actualizan filas.';
COMMENT ON COLUMN transactions.reference          IS 'UUID v4 generado por la aplicación; clave de idempotencia';
COMMENT ON COLUMN transactions.related_account_id IS 'Vincula las dos patas de una transferencia (DEBIT ↔ CREDIT)';
COMMENT ON COLUMN transactions.balance_after      IS 'Saldo de la cuenta origen/destino inmediatamente después del movimiento';

-- Índices de TRANSACTIONS
-- Q2: historial paginado por cuenta, orden descendente
CREATE INDEX idx_transactions_account_created_at  ON transactions (account_id, created_at DESC);
-- Q3: conteo de transferencias por tipo y período
CREATE INDEX idx_transactions_account_type_date   ON transactions (account_id, type, created_at);
-- Navegación desde cuenta relacionada (vista del destinatario)
CREATE INDEX idx_transactions_related_account_id  ON transactions (related_account_id);
