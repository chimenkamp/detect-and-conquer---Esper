#foreach($event in $eventsMap.entrySet())

@Name('ExclusiveChoiceAsStream$event.key')
INSERT INTO ExclusiveChoiceStream
SELECT *
FROM ${EPPMEventType}.win:time($time_window) as event
WHERE event.activity = '$event.value'
HAVING NOT EXISTS (
    SELECT *
    FROM ${EPPMEventType}.win:time($time_window) as subEvent
    WHERE subEvent.caseID = event.caseID
    #foreach($otherEvent in $eventsMap.entrySet())
        #if($otherEvent.key != $event.key)
        AND subEvent.activity = '$otherEvent.value'
    #end
#end
);
#end