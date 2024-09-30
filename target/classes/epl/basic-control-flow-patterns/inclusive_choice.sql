#set($eventKeys = $eventsMap.keySet().toArray())
#set($eventValues = $eventsMap.values().toArray())
#set($eventCount = $eventKeys.size() - 1)

#set($eventValuesList = '')
#foreach($value in $eventValues)
    #if($foreach.count > 1)
        #set($eventValuesList = "$eventValuesList, '$value'")
    #else
        #set($eventValuesList = "'$value'")
    #end
#end

SELECT * FROM PATTERN [
EVERY
#foreach($i in [0..$eventCount])
    ($eventKeys[$i] = $EPPMEventType(activity ='$eventValues[$i]'))
    #if($i < $eventCount)
        OR
    #end
#end
].win:time($time_window)