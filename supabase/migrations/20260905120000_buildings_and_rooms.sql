-- Buildings & rooms master data: lets admins pick a building/room for a class period (free
-- text on timetable_periods) and flag rooms usable as teacher offices (free text on
-- teachers.office_room). Kept as standalone lookup tables (no FK back-fill onto the existing
-- period/teacher text columns) to avoid touching the no-double-booking trigger or existing data.
create table buildings (
  building_id text primary key,
  name        text not null,
  code        text,
  is_active   boolean not null default true,
  created_at  timestamptz not null default now(), created_by text,
  updated_at  timestamptz not null default now(), updated_by text,
  is_deleted  boolean not null default false,
  deleted_at  timestamptz,
  deleted_by  text
);

create table rooms (
  room_id     text primary key,
  building_id text not null references buildings(building_id) on delete cascade,
  room_no     text not null,
  name        text,
  capacity    int,
  is_office   boolean not null default false,
  is_active   boolean not null default true,
  created_at  timestamptz not null default now(), created_by text,
  updated_at  timestamptz not null default now(), updated_by text,
  is_deleted  boolean not null default false,
  deleted_at  timestamptz,
  deleted_by  text,
  unique (building_id, room_no)
);

create index idx_rooms_building_id on rooms (building_id);
create index idx_buildings_updated_at on buildings (updated_at);
create index idx_rooms_updated_at on rooms (updated_at);

drop trigger if exists trg_touch_buildings on buildings;
create trigger trg_touch_buildings before update on buildings for each row execute function public.fn_touch_updated_at();

drop trigger if exists trg_touch_rooms on rooms;
create trigger trg_touch_rooms before update on rooms for each row execute function public.fn_touch_updated_at();

alter table buildings enable row level security;
alter table rooms enable row level security;

create policy sel_buildings on buildings for select to authenticated using (true);
create policy adm_buildings on buildings for all to authenticated
  using (is_admin()) with check (is_admin());

create policy sel_rooms on rooms for select to authenticated using (true);
create policy adm_rooms on rooms for all to authenticated
  using (is_admin()) with check (is_admin());
