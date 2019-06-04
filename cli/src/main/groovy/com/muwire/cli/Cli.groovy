package com.muwire.cli

import com.muwire.core.Core
import com.muwire.core.MuWireSettings
import com.muwire.core.files.FileHashedEvent
import com.muwire.core.files.FileSharedEvent

class Cli {
    
    public static void main(String[] args) {
        def home = System.getProperty("user.home") + File.separator + ".MuWire"
        home = new File(home)
        if (!home.exists())
            home.mkdirs()
        
        def propsFile = new File(home,"MuWire.properties")
        if (!propsFile.exists()) {
            println "create props file ${propsFile.getAbsoluteFile()} before launching MuWire"
            System.exit(1)
        }
        
        def props = new Properties()
        propsFile.withInputStream { props.load(it) }
        props = new MuWireSettings(props)
        
        Core core 
        try {
            core = new Core(props, home, "0.0.8")
        } catch (Exception bad) {
            bad.printStackTrace(System.out)
            println "Failed to initialize core, exiting"
            System.exit(1)
        }
        
        core.startServices()
        
        // now we begin
        println "MuWire is ready"
        
        def filesList
        if (args.length == 0) {
            println "Enter a file containing list of files to share"
            def reader = new BufferedReader(new InputStreamReader(System.in))
            filesList = reader.readLine()
        } else 
            filesList = args[0]
        
        Thread.sleep(1000)
        println "loading shared files from $filesList"
        
        core.eventBus.register(FileHashedEvent.class, new Object() {
            void onFileHashedEvent(FileHashedEvent e) {
                if (e.error != null)
                    println "ERROR $e.error"
                else
                    println "Shared file : $e.sharedFile.file"
            }
        })
        
        filesList = new File(filesList)
        filesList.withReader { 
            def toShare = it.readLine()
            core.eventBus.publish(new FileSharedEvent(file : new File(toShare)))
        }
        
        Thread.sleep(Integer.MAX_VALUE)
    }
}