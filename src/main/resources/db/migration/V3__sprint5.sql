

CREATE TABLE budget
(
    budget_id                   BIGINT AUTO_INCREMENT NOT NULL,
    renovation_id               BIGINT NOT NULL,
    miscellaneous_budget        DECIMAL NULL,
    material_budget             DECIMAL NULL,
    labour_budget               DECIMAL NULL,
    equipment_budget            DECIMAL NULL,
    professional_service_budget DECIMAL NULL,
    permit_budget               DECIMAL NULL,
    cleanup_budget              DECIMAL NULL,
    delivery_budget             DECIMAL NULL,
    CONSTRAINT pk_budget PRIMARY KEY (budget_id)
);

CREATE TABLE chat_channel
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    name          VARCHAR(255) NOT NULL,
    renovation_id BIGINT NULL,
    CONSTRAINT pk_chatchannel PRIMARY KEY (id)
);

CREATE TABLE chat_channel_member
(
    channel_id BIGINT NOT NULL,
    member_id  BIGINT NOT NULL
);

CREATE TABLE chat_message
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    content    TEXT NOT NULL,
    timestamp  datetime     NOT NULL,
    channel_id BIGINT NULL,
    sender_id  BIGINT NULL,
    CONSTRAINT pk_chatmessage PRIMARY KEY (id)
)CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE invitation
    ADD accepted_pending_registration BIT(1) DEFAULT 0;

ALTER TABLE invitation
    ADD expiry_date datetime DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE invitation
    ADD invitation_status SMALLINT DEFAULT 3;

ALTER TABLE invitation
    MODIFY expiry_date datetime NOT NULL;

ALTER TABLE budget
    ADD CONSTRAINT uc_budget_renovation UNIQUE (renovation_id);

ALTER TABLE budget
    ADD CONSTRAINT FK_BUDGET_ON_RENOVATION FOREIGN KEY (renovation_id) REFERENCES renovation (id);

ALTER TABLE chat_channel
    ADD CONSTRAINT FK_CHATCHANNEL_ON_RENOVATION FOREIGN KEY (renovation_id) REFERENCES renovation (id);

ALTER TABLE chat_message
    ADD CONSTRAINT FK_CHATMESSAGE_ON_CHANNEL FOREIGN KEY (channel_id) REFERENCES chat_channel (id);

ALTER TABLE chat_message
    ADD CONSTRAINT FK_CHATMESSAGE_ON_SENDER FOREIGN KEY (sender_id) REFERENCES renovation_user (id);

ALTER TABLE chat_channel_member
    ADD CONSTRAINT fk_chachamem_on_chat_channel FOREIGN KEY (channel_id) REFERENCES chat_channel (id);

ALTER TABLE chat_channel_member
    ADD CONSTRAINT fk_chachamem_on_user FOREIGN KEY (member_id) REFERENCES renovation_user (id);

ALTER TABLE invitation
DROP
COLUMN resolved;

INSERT INTO chat_channel (name, renovation_id)
    SELECT 'general', renovation.id FROM renovation;

INSERT INTO chat_channel_member (channel_id, member_id)
    SELECT chat_channel.id, renovation_member.user_id FROM renovation_member
    JOIN chat_channel ON chat_channel.renovation_id = renovation_member.renovation_id;

INSERT INTO budget (
    renovation_id,
    miscellaneous_budget,
    material_budget,
    labour_budget,
    equipment_budget,
    professional_service_budget,
    permit_budget,
    cleanup_budget,
    delivery_budget
)
SELECT
    renovation.id,
    0, 0, 0, 0, 0, 0, 0, 0
FROM renovation;







