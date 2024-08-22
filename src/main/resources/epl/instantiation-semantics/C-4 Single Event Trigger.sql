#set($eventKey = $eventsMap.keySet().toArray()[0])
#set($eventActivity = $eventsMap.get($eventKey))
@Name('SingleEventTrigger')
select *
from ${EPPMEventType}.win:time($time_window) as event
where event.activity = '$eventActivity'

