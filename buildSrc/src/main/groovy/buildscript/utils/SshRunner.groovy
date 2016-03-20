package buildscript.utils

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class SshRunner extends ProcessRunner{
    String host
    String user
    boolean sudo=true

    File initCmd() {
        if (!cmds) {
            cmds=[cmd]
        }
        def remoteCmdFile=super.initCmd()
        remoteCmdFile.deleteOnExit()

        def cmdFile = File.createTempFile('cmd-', '.sh')
        cmdFile.write([
                "set -e",
                "rsync --checksum --recursive --verbose --delete  --rsh \" ssh -o StrictHostKeyChecking=no \"  $remoteCmdFile.absolutePath $user@$host:",
                "ssh -t -t  -o StrictHostKeyChecking=no $user@$host '${sudo?'sudo ':''}sh /home/$user/$remoteCmdFile.name'",
                "ssh -t -t  -o StrictHostKeyChecking=no $user@$host 'rm /home/$user/$remoteCmdFile.name'",
        ].join('\n'))
        cmd = "bash $cmdFile.absolutePath"
        println("from $user@$host with commands: $cmd\n${cmdFile.text}\n\n")
        cmdFile
    }

}
