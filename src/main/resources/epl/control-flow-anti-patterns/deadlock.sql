@Name('DeadlockDetection')
INSERT INTO DeadlockStream
SELECT ex.caseID, 'DeadlockDetected' AS DeadlockType
FROM ExclusiveChoiceStream.win:time(5 sec) AS ex
INNER JOIN ParallelMergeStream.win:time(5 sec) AS pm
ON ex.caseID = pm.caseID
    OUTPUT LAST EVERY 5 SECONDS;