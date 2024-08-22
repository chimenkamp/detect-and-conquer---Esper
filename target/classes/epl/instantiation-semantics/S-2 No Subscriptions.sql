#set($eventKeys = $eventsMap.keySet().toArray())
#set($eventValues = $eventsMap.values().toArray())
#set($eventCount = $eventKeys.size() - 1)
#set($firstEventKey = $eventsMap.keySet().toArray()[0])
#set($firstActivityValue = $eventsMap.get($firstEventKey))
@Name('NoSubscriptions')
select *
from pattern [
    every e = ${EPPMEventType}(activity = '$firstActivityValue')
    ].win:time($time_window)

