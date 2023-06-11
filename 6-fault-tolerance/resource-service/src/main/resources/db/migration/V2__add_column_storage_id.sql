delete from resources;

alter table resources drop column bucket;

alter table resources add column storage_id bigint not null;
