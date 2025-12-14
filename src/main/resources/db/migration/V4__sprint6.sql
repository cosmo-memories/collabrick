CREATE TABLE IF NOT EXISTS SPRING_AI_CHAT_MEMORY (
    conversation_id VARCHAR(36) NOT NULL,
    content TEXT NOT NULL,
    type ENUM('USER', 'ASSISTANT', 'SYSTEM', 'TOOL') NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX SPRING_AI_CHAT_MEMORY_CONVERSATION_ID_TIMESTAMP_IDX (conversation_id, timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO renovation_user (id, fname, lname, email, password, image, activated)
VALUES (NEXTVAL(renovation_user_seq), 'BrickAI', '', 'brickai@brickaimail.com', '', 'images/BrickAI.png', 1);

INSERT INTO chat_channel_member (channel_id, member_id)
SELECT c.id AS channel_id, u.id AS member_id
FROM chat_channel c
         JOIN renovation_user u ON u.email = 'brickai@brickaimail.com'
WHERE c.id NOT IN (
    SELECT channel_id
    FROM chat_channel_member
    WHERE member_id = u.id
);

CREATE TABLE chat_mention
(
    id                BIGINT AUTO_INCREMENT NOT NULL,
    message_id        BIGINT                NULL,
    mentioned_user_id BIGINT                NULL,
    start_position    INT                   NULL,
    end_position      INT                   NULL,
    seen              BIT(1)                NULL,
    CONSTRAINT pk_chatmention PRIMARY KEY (id)
);

CREATE TABLE live_update
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    user_id       BIGINT                NULL,
    renovation_id BIGINT                NOT NULL,
    timestamp     datetime              NOT NULL,
    task_id       BIGINT                NULL,
    expense_id    BIGINT                NULL,
    invitation_id UUID                  NULL,
    activity_type VARCHAR(255)          NOT NULL,
    CONSTRAINT pk_liveupdate PRIMARY KEY (id)
);

CREATE TABLE recently_accessed_renovation
(
    time_accessed datetime NULL,
    renovation_id BIGINT   NOT NULL,
    user_id       BIGINT   NOT NULL,
    CONSTRAINT pk_recentlyaccessedrenovation PRIMARY KEY (renovation_id, user_id)
);

ALTER TABLE chat_mention
    ADD CONSTRAINT FK_CHATMENTION_ON_MENTIONED_USER FOREIGN KEY (mentioned_user_id) REFERENCES renovation_user (id);

ALTER TABLE chat_mention
    ADD CONSTRAINT FK_CHATMENTION_ON_MESSAGE FOREIGN KEY (message_id) REFERENCES chat_message (id);

ALTER TABLE live_update
    ADD CONSTRAINT FK_LIVEUPDATE_ON_EXPENSE FOREIGN KEY (expense_id) REFERENCES expense (id);

ALTER TABLE live_update
    ADD CONSTRAINT FK_LIVEUPDATE_ON_INVITATION FOREIGN KEY (invitation_id) REFERENCES invitation (id);

ALTER TABLE live_update
    ADD CONSTRAINT FK_LIVEUPDATE_ON_RENOVATION FOREIGN KEY (renovation_id) REFERENCES renovation (id);

ALTER TABLE live_update
    ADD CONSTRAINT FK_LIVEUPDATE_ON_TASK FOREIGN KEY (task_id) REFERENCES task (id);

ALTER TABLE live_update
    ADD CONSTRAINT FK_LIVEUPDATE_ON_USER FOREIGN KEY (user_id) REFERENCES renovation_user (id);

ALTER TABLE recently_accessed_renovation
    ADD CONSTRAINT FK_RECENTLYACCESSEDRENOVATION_ON_RENOVATION FOREIGN KEY (renovation_id) REFERENCES renovation (id);

ALTER TABLE recently_accessed_renovation
    ADD CONSTRAINT FK_RECENTLYACCESSEDRENOVATION_ON_USER FOREIGN KEY (user_id) REFERENCES renovation_user (id);

-- Insert one brickAI chat channel per user per renovation
INSERT INTO chat_channel (name, renovation_id)
SELECT DISTINCT CONCAT('brickAI_', rm.user_id), rm.renovation_id
FROM renovation_member rm;

-- Insert each user as the sole member of their own brickAI channel
INSERT INTO chat_channel_member (channel_id, member_id)
SELECT cc.id, rm.user_id
FROM chat_channel cc
         JOIN renovation_member rm ON cc.renovation_id = rm.renovation_id
WHERE cc.name = CONCAT('brickAI_', rm.user_id)
  AND NOT EXISTS (
    SELECT 1
    FROM chat_channel_member ccm
    WHERE ccm.channel_id = cc.id
      AND ccm.member_id = rm.user_id
);

-- Update channel names to 'brickAI'
UPDATE chat_channel
SET name = 'brickAI'
WHERE name LIKE 'brickAI_%';

INSERT INTO chat_channel_member (channel_id, member_id)
SELECT cc.id, ru.id
FROM chat_channel cc
         JOIN renovation_user ru ON ru.fname = 'BrickAI'
         LEFT JOIN chat_channel_member ccm
                   ON ccm.channel_id = cc.id AND ccm.member_id = ru.id
WHERE cc.name = 'brickAI'
  AND ccm.member_id IS NULL;


