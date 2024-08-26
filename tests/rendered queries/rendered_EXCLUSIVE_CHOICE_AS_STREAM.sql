
@Name('ExclusiveChoiceAsStreamactivity_2')
INSERT INTO ExclusiveChoiceStream
SELECT *
FROM ubt.process_analytics.esper.EPPMEventType.win:time(50 sec) as event
WHERE event.activity = 'Electronic invoice received'
HAVING NOT EXISTS (
    SELECT *
    FROM ubt.process_analytics.esper.EPPMEventType.win:time(50 sec) as subEvent
    WHERE subEvent.caseID = event.caseID
        AND subEvent.activity = 'Paper invoice received'
);

@Name('ExclusiveChoiceAsStreamactivity_1')
INSERT INTO ExclusiveChoiceStream
SELECT *
FROM ubt.process_analytics.esper.EPPMEventType.win:time(50 sec) as event
WHERE event.activity = 'Paper invoice received'
HAVING NOT EXISTS (
    SELECT *
    FROM ubt.process_analytics.esper.EPPMEventType.win:time(50 sec) as subEvent
    WHERE subEvent.caseID = event.caseID
        AND subEvent.activity = 'Electronic invoice received'
);

