@Name('ParallelMergeAsStream')
INSERT INTO ParallelMergeStream
SELECT *
FROM ubt.process_analytics.esper.EPPMEventType.win:time(50 sec) as event
WHERE event.activity IN (
    'Electronic invoice received'
,    'Paper invoice received'
    )
GROUP BY event.caseID
HAVING COUNT(DISTINCT event.activity) = 2;
