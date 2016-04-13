set serveroutput on size 30000;
drop table orig_tab;
CREATE TABLE orig_tab (
PK     NUMBER(8)       not null,
NAME   VARCHAR2(20)
);

INSERT INTO orig_tab (PK, NAME) VALUES (1, 'AAA');
INSERT INTO orig_tab (PK, NAME) VALUES (2, 'BBB');
INSERT INTO orig_tab (PK, NAME) VALUES (3, 'CCC');
COMMIT;


drop trigger vr_reporting_trigger;
CREATE OR REPLACE TRIGGER vr_reporting_trigger
    after UPDATE ON orig_tab
    FOR EACH ROW
  BEGIN
    IF UPDATING('PK') THEN
       DBMS_OUTPUT.put_line ('PK UPDATED'  );
    END IF;
    IF UPDATING('NAME') THEN
       DBMS_OUTPUT.put_line ('Name UPDATED '|| 'Old Value '|| :old.Name || ' New Value ' || :new.Name  );
    END IF;
  END;
  /
  
truncate table orig_tab;
  rollback;
select * from orig_tab;
update orig_tab set Name='DDD',PK=4 
update orig_tab set Name='CCC' where NAME='DDD';
rollback
  
 
update orig_tab set Name='DDD' where NAME='CCC';
update orig_tab set Name='CCC' where NAME='DDD';
Commit;

insert into ZZZ_TMP_COL_UPDATE values ("asasda");
drop table ZZZ_TMP_COL_UPDATE;

purge recyclebin;


select * from tab;


create or replace TRIGGER col_tri_BONUS after UPDATE ON BONUS FOR EACH ROW  BEGIN 
IF UPDATING('COMM') THEN 
insert into ZZZ_TMP_COL_UPDATE values ('BONUS::COMM:old.COMM:new.COMM')
END IF; 
END;
/


--select 'drop trigger ' || trigger_name || ';' stmt from user_triggers;