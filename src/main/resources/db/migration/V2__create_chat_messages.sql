create table chat_messages (
    id uuid primary key,
    room_id varchar(120) not null,
    sender varchar(120) not null,
    text varchar(2000) not null,
    sent_at timestamp not null
);

create index idx_chat_messages_room_sent_at on chat_messages (room_id, sent_at desc);
