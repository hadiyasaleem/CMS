-- A college-wide base fee structure, one per shift (Morning and Evening can differ). Every session's fee
-- structure for that shift follows the base until an admin saves a structure of its own for the session
-- (an override); removing the override puts the session back on the base. The apps resolve this in the
-- same way for the fee screen, the student's challan and the exports.
create table college_fees (
  shift         shift primary key,
  cadence       fee_cadence not null,
  academic_year text,
  due_date      date,
  payment_note  text default 'Payable at the college accounts office (accountant), not a bank',
  created_at    timestamptz not null default now(),
  created_by    text,
  updated_at    timestamptz not null default now(),
  updated_by    text,
  is_deleted    boolean not null default false,
  deleted_at    timestamptz,
  deleted_by    text
);

create table college_fee_heads (
  shift      shift not null references college_fees(shift) on delete cascade,
  label      text not null,
  amount     numeric(10,2) not null,
  position   int not null default 0,
  created_at timestamptz not null default now(),
  created_by text,
  updated_at timestamptz not null default now(),
  updated_by text,
  is_deleted boolean not null default false,
  deleted_at timestamptz,
  deleted_by text,
  primary key (shift, label)
);

alter table college_fees enable row level security;
alter table college_fee_heads enable row level security;

-- Everyone signed in reads the base (students need it for their challan); only admins change it.
create policy sel_college_fees on college_fees for select to authenticated using (true);
create policy ins_college_fees on college_fees for insert to authenticated with check (is_admin());
create policy upd_college_fees on college_fees for update to authenticated using (is_admin()) with check (is_admin());
create policy del_college_fees on college_fees for delete to authenticated using (is_admin());

create policy sel_college_fee_heads on college_fee_heads for select to authenticated using (true);
create policy ins_college_fee_heads on college_fee_heads for insert to authenticated with check (is_admin());
create policy upd_college_fee_heads on college_fee_heads for update to authenticated using (is_admin()) with check (is_admin());
create policy del_college_fee_heads on college_fee_heads for delete to authenticated using (is_admin());

create trigger trg_audit_college_fees before insert or update on college_fees
  for each row execute function fn_cms_audit_row();
create trigger trg_touch_college_fees before update on college_fees
  for each row execute function fn_touch_updated_at();
create trigger trg_audit_college_fee_heads before insert or update on college_fee_heads
  for each row execute function fn_cms_audit_row();
create trigger trg_touch_college_fee_heads before update on college_fee_heads
  for each row execute function fn_touch_updated_at();

create index idx_college_fees_updated_at on college_fees(updated_at);
create index idx_college_fee_heads_updated_at on college_fee_heads(updated_at);
