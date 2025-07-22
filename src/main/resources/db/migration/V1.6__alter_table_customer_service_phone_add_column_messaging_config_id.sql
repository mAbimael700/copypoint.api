ALTER TABLE "customer_service_phones"
ADD COLUMN "messaging_config_id" bigint NOT NULL;

ALTER TABLE "customer_service_phones"
ADD CONSTRAINT "fk_customer_service_phones_references_messaging_provider_configurations"
FOREIGN KEY ("messaging_config_id")
REFERENCES "messaging_provider_configurations"("id");
