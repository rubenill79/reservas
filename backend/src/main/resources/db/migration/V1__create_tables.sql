   CREATE TABLE resource (
       id   BIGSERIAL PRIMARY KEY,
       name VARCHAR(100) NOT NULL UNIQUE
   );

   CREATE TABLE booking (
       id          BIGSERIAL PRIMARY KEY,
       resource_id BIGINT NOT NULL REFERENCES resource(id),
       start_time  TIMESTAMP NOT NULL,
       end_time    TIMESTAMP NOT NULL,
       CHECK (end_time > start_time)
   );