create table if not exists songs (
    id bigserial primary key,
    name text not null,
    artist text not null,
    album text,
    length text not null,
    released text
);
