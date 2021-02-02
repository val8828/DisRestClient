# DGW
 DGW is a service for connecting sql base and intraservice tasktracker. 
 DGW contains some parameters which may be changed from commandline.
 To know list of all parameters type in cmd: "java -jar [path of DGW.jar] -h" .
 DGW reading alarm list from sql base, filtrating it and create tasks in intraservice.
 To change parameters just call in cmd program with new arguments, old parameters will be replaced.
 Program create log file in the same directory and write errors and info message to it.
 When program using first time - need to call with all login and passwords(from sql base and intraservice).
 If error ocurried DGW send email with alarm event.
