-- Read-only verification; no password/hash/token columns selected.
-- Version: v0.2; Status: Draft; Project: v0.4; Updated: 2026-11-14.
-- Source: application.yml defaults, JPA entities/repositories; see profile-502-investigation.md.
-- Confirm schemas first; defaults in application.yml shown below.
SELECT table_schema, table_name, column_name
FROM information_schema.columns
WHERE table_schema IN ('rentwise_training_v04','rentwise_profile_v04')
AND table_name IN ('users','diagnosis_session','answer_record','user_mastery','profile_sync_checkpoint')
ORDER BY table_schema, table_name, ordinal_position;

-- user 8: profile_probe_1794637005805, intentionally never diagnosed.
-- user 7: ui01_1794636831873, completed diagnosis/training/assessment.
SELECT id, username FROM rentwise_training_v04.users WHERE id IN (7,8);
SELECT user_id, COUNT(*) AS profile_rows
FROM rentwise_profile_v04.user_mastery WHERE user_id IN (7,8) GROUP BY user_id;
SELECT COUNT(*) AS undiagnosed_user_8_profile_rows
FROM rentwise_profile_v04.user_mastery WHERE user_id=8;
SELECT user_id, topic, mastery_score, consecutive_wrong, last_difficulty
FROM rentwise_profile_v04.user_mastery WHERE user_id IN (7,8) ORDER BY user_id,topic;
SELECT user_id, source, COUNT(*) AS answer_count
FROM rentwise_training_v04.answer_record WHERE user_id IN (7,8) GROUP BY user_id,source;
SELECT id, user_id, session_type, status, started_at, finished_at
FROM rentwise_training_v04.diagnosis_session WHERE user_id IN (7,8) ORDER BY id;
SELECT id, user_id, session_id, case_id, topic, difficulty, correct, source
FROM rentwise_training_v04.answer_record WHERE user_id=7 AND (id=60 OR session_id=8) ORDER BY id;
SELECT user_id,last_answer_id,row_version
FROM rentwise_profile_v04.profile_sync_checkpoint WHERE user_id IN (7,8);

-- Optional follow-up for the post-fix regression account (not yet checked in Workbench):
-- user 9: profile_fix_1794637969189; diagnosis session 9, training answer 76, assessment session 10.
SELECT id, username FROM rentwise_training_v04.users WHERE id=9;
SELECT COUNT(*) AS profile_rows FROM rentwise_profile_v04.user_mastery WHERE user_id=9;
SELECT source, COUNT(*) AS answer_count FROM rentwise_training_v04.answer_record WHERE user_id=9 GROUP BY source;
