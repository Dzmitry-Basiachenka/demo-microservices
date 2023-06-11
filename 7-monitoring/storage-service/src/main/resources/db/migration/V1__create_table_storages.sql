create table if not exists storages (
    id bigserial primary key,
    type text not null,
    bucket text not null
);
