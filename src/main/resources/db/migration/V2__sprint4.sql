CREATE SEQUENCE expense_seq INCREMENT BY 50 START WITH 1;

CREATE TABLE expense
(
    id               BIGINT       NOT NULL,
    expense_name     VARCHAR(255) NOT NULL,
    expense_category VARCHAR(255) NOT NULL,
    expense_cost     DECIMAL      NOT NULL,
    expense_date     date         NOT NULL,
    task_id          BIGINT       NOT NULL,
    CONSTRAINT pk_expense PRIMARY KEY (id)
);

CREATE TABLE invitation
(
    id            UUID   NOT NULL,
    user_id       BIGINT       NULL,
    email         VARCHAR(255) NULL,
    renovation_id BIGINT       NOT NULL,
    resolved      BIT(1)       NULL,
    CONSTRAINT pk_invitation PRIMARY KEY (id)
);

CREATE TABLE renovation_member
(
    `role`        SMALLINT NOT NULL,
    renovation_id BIGINT   NOT NULL,
    user_id       BIGINT   NOT NULL,
    CONSTRAINT pk_renovationmember PRIMARY KEY (renovation_id, user_id)
);

ALTER TABLE forgotten_password_token
    ADD CONSTRAINT uc_forgottenpasswordtoken_user UNIQUE (user_id);

ALTER TABLE verification_token
    ADD CONSTRAINT uc_verificationtoken_token UNIQUE (token);

ALTER TABLE verification_token
    ADD CONSTRAINT uc_verificationtoken_user UNIQUE (user_id);

CREATE UNIQUE INDEX IX_pk_tag ON tag (tag, renovation);

ALTER TABLE authority
    ADD CONSTRAINT FK_AUTHORITY_ON_USER FOREIGN KEY (user_id) REFERENCES renovation_user (id);

ALTER TABLE expense
    ADD CONSTRAINT FK_EXPENSE_ON_TASK FOREIGN KEY (task_id) REFERENCES task (id);

ALTER TABLE forgotten_password_token
    ADD CONSTRAINT FK_FORGOTTENPASSWORDTOKEN_ON_USER FOREIGN KEY (user_id) REFERENCES renovation_user (id);

ALTER TABLE invitation
    ADD CONSTRAINT FK_INVITATION_ON_RENOVATION FOREIGN KEY (renovation_id) REFERENCES renovation (id);

ALTER TABLE invitation
    ADD CONSTRAINT FK_INVITATION_ON_USER FOREIGN KEY (user_id) REFERENCES renovation_user (id);

ALTER TABLE renovation_member
    ADD CONSTRAINT FK_RENOVATIONMEMBER_ON_RENOVATION FOREIGN KEY (renovation_id) REFERENCES renovation (id);

ALTER TABLE renovation_member
    ADD CONSTRAINT FK_RENOVATIONMEMBER_ON_USER FOREIGN KEY (user_id) REFERENCES renovation_user (id);

ALTER TABLE renovation
    ADD CONSTRAINT FK_RENOVATION_ON_USER FOREIGN KEY (user_id) REFERENCES renovation_user (id);

ALTER TABLE room
    ADD CONSTRAINT FK_ROOM_ON_RENOVATION FOREIGN KEY (renovation_id) REFERENCES renovation (id);

ALTER TABLE tag
    ADD CONSTRAINT FK_TAG_ON_RENOVATION FOREIGN KEY (renovation) REFERENCES renovation (id);

ALTER TABLE task
    ADD CONSTRAINT FK_TASK_ON_RENOVATION FOREIGN KEY (renovation_id) REFERENCES renovation (id);

ALTER TABLE verification_token
    ADD CONSTRAINT FK_VERIFICATIONTOKEN_ON_USER FOREIGN KEY (user_id) REFERENCES renovation_user (id);

ALTER TABLE task_room
    ADD CONSTRAINT fk_task_room_on_room FOREIGN KEY (room_id) REFERENCES room (id);

ALTER TABLE task_room
    ADD CONSTRAINT fk_task_room_on_task FOREIGN KEY (task_id) REFERENCES task (id);

ALTER TABLE task
DROP COLUMN state;

ALTER TABLE task
    ADD state VARCHAR(255) NOT NULL;

INSERT INTO renovation_member (role, renovation_id, user_id)
SELECT 0, id, user_id
FROM renovation;
