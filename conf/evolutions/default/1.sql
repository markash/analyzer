# --- !Ups
create table if not exists settings (
  host varchar(256) not null,
  port int not null
);

# --- !Downs
drop table if exists settings;