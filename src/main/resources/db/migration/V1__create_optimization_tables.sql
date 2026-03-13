CREATE TABLE optimization_result (
    id              UUID PRIMARY KEY,
    max_volume      DOUBLE PRECISION    NOT NULL,
    total_volume    DOUBLE PRECISION    NOT NULL,
    total_revenue   DOUBLE PRECISION    NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE selected_shipment (
    id                      BIGSERIAL PRIMARY KEY,
    optimization_result_id  UUID NOT NULL
        REFERENCES optimization_result(id) ON DELETE CASCADE,
    name                    VARCHAR(255) NOT NULL,
    volume                  DOUBLE PRECISION NOT NULL,
    revenue                 DOUBLE PRECISION NOT NULL
);

CREATE INDEX idx_selected_shipment_result_id
    ON selected_shipment(optimization_result_id);

CREATE INDEX idx_optimization_result_created_at
    ON optimization_result(created_at);
