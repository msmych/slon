create table if not exists migration_test
(
    id serial primary key
);

insert into migration_test (id)
values (1);