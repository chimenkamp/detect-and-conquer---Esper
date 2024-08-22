#set($eventKeys = $eventsMap.keySet().toArray())
#set($eventValues = $eventsMap.values().toArray())
#set($eventCount = $eventKeys.size() - 1)
#set($firstEventKey = $eventsMap.keySet().toArray()[0])
#set($firstActivityValue = $eventsMap.get($firstEventKey))

@Name('UntilConsumption')
select *
from pattern [
    every (
    ${eventKeys[0]}_init = ${EPPMEventType}(activity = '${eventsMap.get($eventsMap.keySet().iterator().next())}')
    #foreach($i in [0..$eventCount])
    -> ${eventKeys[$i]} = ${EPPMEventType}(activity = '${eventValues[$i]}')
    #end
    )
    ].win:time($time_window)
