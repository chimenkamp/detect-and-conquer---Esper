@Name('ParallelMergeAsStream')
INSERT INTO ParallelMergeStream
SELECT *
FROM ${EPPMEventType}.win:time($time_window) as event
WHERE event.activity IN (
    #foreach($event in $eventsMap.entrySet())
    '$event.value'
    #if($foreach.hasNext),#end
    #end
    )
GROUP BY event.caseID
HAVING COUNT(DISTINCT event.activity) = $eventsMap.size();