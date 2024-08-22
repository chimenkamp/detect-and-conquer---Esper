@Name('EventBasedUnsubscription')
select *
from pattern [
    every (
    e1 = ${EPPMEventType}(activity = '${eventsMap.get($eventsMap.keySet().iterator().next())}')
    -> e2 = ${EPPMEventType}(activity = '${eventsMap.get($eventsMap.keySet().iterator().next())}')
    #foreach($eventKey in $eventsMap.keySet())
    -> not ${EPPMEventType}(activity = '${eventsMap.get($eventKey)}')
    #end
    )
    ]

