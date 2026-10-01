-- Fines and the fee structure's late fine note were removed from every app. Their data goes with them.
-- Dropping the table also removes its policies, its updated_at trigger and its indexes.
drop table if exists fines;

alter table session_fees drop column if exists late_fine_note;

drop type if exists fine_category;
