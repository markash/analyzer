# --- !Ups
create table if not exists counter (
  objectName varchar(512) not null,
  name varchar(128) not null,
  value int not null
);

# --- !Downs
drop table if exists counter;