create table series
(
    id int generated by default as identity,
    title text,
    released date not null,
    finale date,
    runtime time,
    plot text,
    rated text,
    awards text,
    total_seasons int,
    tid int,
    poster_img bytea,
    constraint series_pk primary key (id),
    constraint series_type_fk foreign key (tid) references type (id)
);

create table episode
(
    id int generated by default as identity,
    title text,
    year date,
    rate text,
    season int,
    episode int,
    runtime time,
    plot text,
    tid int,
    poster_img bytea,
    sid int,
    constraint episode_pk primary key (id),
    constraint episode_type_fk foreign key (tid) references type (id),
    constraint episode_series_fk foreign key (sid) references series (id)
);

create table writer
(
    id int generated by default as identity,
    first_name text not null,
    last_name text,
    constraint writer_pk primary key (id)
);

create table episode_ratings
(
    eid int,
    aid int, -- agency id
    val float, -- normalized
    constraint episode_ratings_pk primary key (eid, aid),
    constraint episode_ratings_episode_fk foreign key (eid) references episode (id),
    constraint episode_ratings_ratings_fk foreign key (aid) references ratings_agency (id)
);

create table episode_director
(
    eid int,
    did int,
    constraint episode_director_pk primary key (eid, did),
    constraint episode_director_episode_fk foreign key (eid) references episode (id),
    constraint episode_director_director_fk foreign key (did) references director (id)
);

create table episode_genre
(
    eid int,
    gid int,
    constraint episode_genre_pk primary key (eid, gid),
    constraint episode_genre_episode_fk foreign key (eid) references episode (id),
    constraint episode_genre_genre_fk foreign key (gid) references genre (id)
);

create table episode_writer
(
    eid int,
    wid int,
    constraint episode_writer_pk primary key (eid, wid),
    constraint episode_writer_episode_fk foreign key (eid) references episode (id),
    constraint episode_writer_writer_fk foreign key (wid) references writer (id)
);

create table episode_actor
(
    eid int,
    aid int,
    constraint episode_actor_pk primary key (eid, aid),
    constraint episode_actor_episode_fk foreign key (eid) references episode (id),
    constraint episode_actor_actor_fk foreign key (aid) references actor (id)
);

create table series_actor
(
    sid int,
    aid int,
    constraint series_actor_pk primary key (sid, aid),
    constraint series_actor_series_fk foreign key (sid) references series (id),
    constraint series_actor_actor_fk foreign key (aid) references actor (id)
);

create table series_writer
(
    sid int,
    wid int,
    constraint series_writer_pk primary key (sid, wid),
    constraint series_writer_series_fk foreign key (sid) references series (id),
    constraint series_writer_writer_fk foreign key (wid) references writer (id)
);

create table series_lang
(
    sid int,
    lid int,
    constraint series_lang_pk primary key (sid, lid),
    constraint series_lang_series_fk foreign key (sid) references series (id),
    constraint series_lang_lang_fk foreign key (lid) references series (id)
);

create table series_ratings
(
    sid int,
    aid int, -- agency id
    val float, -- normalized
    constraint series_ratings_pk primary key (sid, aid),
    constraint series_ratings_series_fk foreign key (sid) references series (id),
    constraint series_ratings_agency_fk foreign key (aid) references ratings_agency (id)
);