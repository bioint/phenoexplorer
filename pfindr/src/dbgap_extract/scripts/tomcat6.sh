#!/bin/bash

WEEK="week_$(expr $(date +%W) % 4)"
SUBJECT="PhenoExplorer Tomcat (${WEEK})"
EMAIL="ambite@isi.edu serban@isi.edu tallis@isi.edu"

pfindr_home=/pfindr/users/pfindr
MAILMESSAGE="${pfindr_home}/update_dbgap/tomcatMessage.txt"
echo -n "" > ${MAILMESSAGE}

sendMail() {
	echo $1 >> ${MAILMESSAGE}
	/bin/mail -s "${SUBJECT}" "${EMAIL}" < ${MAILMESSAGE}
}

# wait the dbGaP update process to finish
(
flock 200

echo "$(date): The dbGaP update process has finished." >> ${MAILMESSAGE}

) 200</var/lock/pfindr.lock

/sbin/service tomcat6 stop
if [ $? != 0 ]
	then
		sendMail "Can not stop the Tomcat server."
		exit 1
	fi

echo "$(date): The Tomcat Server was stopped" >> ${MAILMESSAGE}

sleep 60

/sbin/service tomcat6 start
if [ $? != 0 ]
	then
		sendMail "Can not start the Tomcat server."
		exit 1
	fi

echo "$(date): The Tomcat Server was started" >> ${MAILMESSAGE}
sendMail "The Tomcat server was successfully restarted."
exit 0
