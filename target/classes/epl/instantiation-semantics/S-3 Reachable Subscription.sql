#set($eventKeys = $eventsMap.keySet().toArray())
#set($eventValues = $eventsMap.values().toArray())
#set($eventCount = $eventKeys.size() - 1)
#set($firstEventKey = $eventsMap.keySet().toArray()[0])
#set($firstActivityValue = $eventsMap.get($firstEventKey))
@Name('ReachableSubscriptions')
select *
from pattern [
    every e = ${EPPMEventType}(activity = '${eventsMap.get($eventsMap.keySet().iterator().next())}')
    ].win:time($time_window);

insert into SubscriptionStream
select *
from ${EPPMEventType} as e
where e.activity in (
                   '${eventsMap.get($eventsMap.keySet().iterator().next())}' #foreach($eventKey in $eventsMap.keySet())
    , '${eventsMap.get($eventKey)}' #end
    )
