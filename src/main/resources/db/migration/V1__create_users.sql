create table users (
    id uuid primary key,
    email varchar(320) not null unique,
    display_name varchar(120) not null,
    created_at timestamp not null default current_timestamp
);

create index idx_users_email on users (email);
