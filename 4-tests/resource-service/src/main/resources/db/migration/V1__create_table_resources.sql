create table if not exists resources (
    id bigserial primary key,
    bucket text not null,
    key text not null,
    name text not null,
    size bigint not null
);
