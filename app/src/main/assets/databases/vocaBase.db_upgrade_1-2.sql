CREATE TABLE Meaning (
  id integer primary key autoincrement,
  wid integer not null references Word(wid) on update cascade on delete cascade,
  meaning not null
);

DROP TABLE Book;
CREATE TABLE Book (
  bid integer primary key autoincrement,
  name unique not null,
  last_modified default CURRENT_DATE
);
