create table users (
    username text,
    password text not null,
    constraint pk_user primary key (username)
)