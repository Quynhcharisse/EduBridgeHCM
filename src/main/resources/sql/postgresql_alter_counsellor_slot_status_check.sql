-- Chạy thủ công trên PostgreSQL (một lần) khi unassign báo:
-- ERROR: new row for relation "counsellor_slot" violates check constraint "counsellor_slot_status_check"
--
-- Hibernate @Enumerated(STRING) lưu tên hằng Java (vd. SLOT_UNASSIGNED), không lưu value "slot_unassigned".

ALTER TABLE counsellor_slot DROP CONSTRAINT IF EXISTS counsellor_slot_status_check;

ALTER TABLE counsellor_slot ADD CONSTRAINT counsellor_slot_status_check
CHECK (status IN (
    'AVAILABLE',
    'BOOKED',
    'DISABLED',
    'CANCELLED',
    'SLOT_UNASSIGNED'
));
