#set($eventKeys = $eventsMap.keySet().toArray())
#set($eventValues = $eventsMap.values().toArray())
#set($eventCount = $eventKeys.size() - 1)
#set($firstEventKey = $eventsMap.keySet().toArray()[0])
#set($firstActivityValue = $eventsMap.get($firstEventKey))
@Name('AllSubscriptions')
select *
from pattern [
    every e = ${EPPMEventType}(activity = '$firstActivityValue')
    ].win:time($time_window);

insert into SubscriptionStream
select *
from ${EPPMEventType} as e
where e.activity in (
                   #foreach($i in [0..$eventCount])
                       '${eventValues[$i]}'#if($i < $eventCount),#end
                       #end
    )
