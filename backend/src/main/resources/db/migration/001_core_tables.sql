create table if not exists sys_user (
    id bigint primary key,
    username varchar(64) not null unique,
    password varchar(128) not null,
    nickname varchar(64),
    status smallint not null default 1,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);

create table if not exists kb_knowledge_base (
    id bigint primary key,
    name varchar(128) not null,
    description varchar(500),
    status smallint not null default 1,
    created_by bigint,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);

create table if not exists kb_document (
    id bigint primary key,
    knowledge_base_id bigint not null,
    file_name varchar(255) not null,
    file_type varchar(32),
    file_size bigint,
    status varchar(32) not null,
    original_path varchar(500),
    chunk_count int default 0,
    created_by bigint,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);

create table if not exists chat_session (
    id bigint primary key,
    user_id bigint not null,
    title varchar(255),
    status smallint not null default 1,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);

create table if not exists chat_message (
    id bigint primary key,
    session_id bigint not null,
    role varchar(32) not null,
    content text not null,
    model_name varchar(64),
    token_usage int,
    created_at timestamp not null default current_timestamp
);

create table if not exists ai_model_config (
    id bigint primary key,
    model_code varchar(64) not null unique,
    model_name varchar(128) not null,
    provider varchar(64) not null,
    base_url varchar(255),
    api_key varchar(255),
    temperature numeric(4, 2),
    max_tokens int,
    enabled smallint not null default 1,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);

create index if not exists idx_kb_document_knowledge_base_id
    on kb_document (knowledge_base_id);

create index if not exists idx_chat_session_user_id_status
    on chat_session (user_id, status);

create index if not exists idx_chat_message_session_id_created_at
    on chat_message (session_id, created_at);

create index if not exists idx_ai_model_config_enabled
    on ai_model_config (enabled);
