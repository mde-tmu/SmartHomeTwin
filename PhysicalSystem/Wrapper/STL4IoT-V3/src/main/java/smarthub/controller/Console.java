package smarthub.controller;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.yakindu.core.ITimerService;
//import smarthub.core.*;
import smarthub.core.ReportLogSaver;
import smarthub.core.ScaledTimeTimerService;
import smarthub.core.Simulation;
import smarthub.core.Tester;
import smarthub.java.*;
import smarthub.ui.Statecharts_Initializer;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class Console {
    private DocumentReference System_Control,
            SmartFireSystemDB,
            SmartTVSystemDB,
            SmartLightSystemDB,
            SmartMicrowaveSystemDB,
            SmartGarageDoorSystemDB;

    public static Clip clip;
    public static AudioInputStream inputStream;
    public static Random r = new Random();
    public Thread anti_freeze = new Thread();

    private static final DecimalFormat df = new DecimalFormat("0.00");

    protected ITimerService timerService;

    public int totalSystemsON = 0,
            SF_Power_kWh = 20,
            STV_Power_kWh = 50,
            SL_Power_kWh = 15,
            SMW_Power_kWh = 10,
            SGD_Power_kWh = 10,
            Power_Manager_Threshold = 90,
            TV_input_index = 1;

    public static double total_HUB_power, current_Power;

    protected static long FlameSensor_detection = 14, SmokeSensor_detection = 8, HeatSensor_detection = 10;

    protected boolean fireAlarm_status;
    protected boolean fireAlarm_sensors_triggered;
    protected boolean TV_status;
    protected boolean TV_usage;
    protected boolean Light_status;
    protected boolean Light_usage;
    protected boolean Microwave_status;
    protected boolean Microwave_usage;
    protected boolean GarageDoor_status;
    protected boolean GarageDoor_usage;
    protected boolean GarageDoor_block;
    protected boolean Main_Switch_DB;
    protected boolean SF_status_DB;
    protected boolean STV_status_DB;
    protected boolean SL_status_DB;
    protected boolean SMW_status_DB;
    protected boolean SGD_status_DB;
    protected boolean firebase_door_blocked;
    protected boolean firebase_status_door;
    protected boolean firebase_garage_door_sync;

    public static ArrayList<String> LogReport = new ArrayList<>(),
            SensorLog = new ArrayList<>();


    public static SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    public static Date date = new Date();

    //Statecharts
    public static smarthub.java.SmartHubSystem SmartHubSystem;
    public static smarthub.java.SmartFireSystem SmartFireSystem;
    public static smarthub.java.SmartTVSystem SmartTVSystem;
    public static smarthub.java.SmartLightHUB SmartLightHUB;
    public static smarthub.java.SmartMicrowaveSystem SmartMicrowaveSystem;
    public static SmartGarageDoorSystem SmartGarageDoorSystem;

    protected static Environment Environment;

    //Atomic Components used in the Smart Systems
    protected static Actuator_Component SF_Actuator, STV_Actuator, SL_Actuator, SMW_Actuator, MotionDetector_Actuator;
    protected static Actuator_Component_2 SGD_Actuator;
    protected static Controller_Component SF_Controller, STV_Controller, SL_Controller, SMW_Controller, MotionDetector_Controller;
    protected static Controller_Component_2 SGD_Controller;
    protected static Sensor_Component FlameSensor, HeatSensor, SmokeSensor, STV_Sensor, SMW_Sensor, SGD_Sensor;
    protected static DeviceTemp_Component SF_Temperature, STV_Temperature, SL_Temperature, SMW_Temperature, SGD_Temperature;
    protected static Power_Component SF_Power, STV_Power, SL_Power, SMW_Power, SGD_Power, MotionDetector_Power;
    protected static Network_Component SF_WiFi, STV_WiFi, SL_WiFi, SMW_WiFi, SGD_WiFi, HUB_WiFi;

    protected static Ultrasonic_MotionDetector US_MotionDetector, GD_MotionDetector;
    protected static Ultrasonic_Sensor US_Sensor, GD_Sensor;

    //HUB Manager
    protected Hub_PowerManager HUBPowerManager;

    protected FireAlarm_Unit FireAlarm;
    protected TV_Unit TV;
    protected LEDLight_Unit LED1, LED2;
    protected SmartLightSystem Light1, Light2; //Each light system using IoT Template
    protected Microwave_Unit Microwave;
    protected GarageDoor_Unit GarageDoor;


    protected ITimerService timer;


    public static Console application = new Console();
    public static boolean allsystem_status = false,
            SF_status = false,
            STV_status = false,
            SL_status = false,
            SMW_status = false,
            SGD_status = false;

    public static void main(String[] args) throws IOException {
        application.runStatecharts();
    }

    public void init_db() throws IOException {
        InputStream serviceAccount = new FileInputStream("src/main/resources/smarthubsystem-firebase-adminsdk-95drd-2c7cda4a2d.json");
        GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(credentials)
                .build();
        FirebaseApp.initializeApp(options);

        Firestore db = FirestoreClient.getFirestore();
        System_Control = db.collection("HubSystem").document("System_Control");
        SmartFireSystemDB = db.collection("HubSystem").document("SmartFireSystem");
        SmartTVSystemDB = db.collection("HubSystem").document("SmartTVSystem");
        SmartLightSystemDB = db.collection("HubSystem").document("SmartLightSystem");
        SmartMicrowaveSystemDB = db.collection("HubSystem").document("SmartMicrowaveSystem");
        SmartGarageDoorSystemDB = db.collection("HubSystem").document("SmartGarageDoorSystem");
    }

    public void runStatecharts() throws IOException {
        LogReport.add("["+formatter.format(new Date())+")]: Application Initializing...\n");
        application.init_db();

        LogReport.add("["+formatter.format(new Date())+")]: Application Initialized...\n");
        LogReport.add("["+formatter.format(new Date())+")]: Application Initialized...\n");

        LogReport.add("["+formatter.format(new Date())+")]: Setting up statemachine...\n");
        application.setupStatemachine();
        LogReport.add("["+formatter.format(new Date())+")]: Statemachine Ready...\n");

        LogReport.add("["+formatter.format(new Date())+")]: Setting up application...\n");
        application.run();
        LogReport.add("["+formatter.format(new Date())+")]: Application Running...\n");
    }

    //Setting up all of the statechart java functions
    protected void setupStatemachine() {

        //Declaring all Statechart Functions
        SmartHubSystem = new SmartHubSystem();
        HUBPowerManager = new Hub_PowerManager();
        Environment = new Environment();


        SmartFireSystem = new SmartFireSystem();
        SmartTVSystem = new SmartTVSystem();
        SmartLightHUB = new SmartLightHUB();
        SmartMicrowaveSystem = new SmartMicrowaveSystem();
        SmartGarageDoorSystem = new SmartGarageDoorSystem();

        FireAlarm = new FireAlarm_Unit();
        TV = new TV_Unit();
        LED1 = new LEDLight_Unit();
        LED2 = new LEDLight_Unit();
        Light1 = new SmartLightSystem();
        Light2 = new SmartLightSystem();
        Microwave = new Microwave_Unit();
        GarageDoor = new GarageDoor_Unit();


        //END OF MAIN SYSTEM and UNIT STATECHART FUNCTIONS

        //ATOMIC COMPONENTS
        SF_Actuator = new Actuator_Component();
        STV_Actuator = new Actuator_Component();
        SL_Actuator = new Actuator_Component();
        SMW_Actuator = new Actuator_Component();
        SGD_Actuator = new Actuator_Component_2();
        MotionDetector_Actuator = new Actuator_Component();

        SF_Controller = new Controller_Component();
        STV_Controller = new Controller_Component();
        SL_Controller = new Controller_Component();
        SMW_Controller = new Controller_Component();
        SGD_Controller = new Controller_Component_2();
        MotionDetector_Controller = new Controller_Component();

        SmokeSensor = new Sensor_Component();
        FlameSensor = new Sensor_Component();
        HeatSensor = new Sensor_Component();
        STV_Sensor = new Sensor_Component();
        SMW_Sensor = new Sensor_Component();
        SGD_Sensor = new Sensor_Component();
        US_Sensor = new Ultrasonic_Sensor();
        US_MotionDetector = new Ultrasonic_MotionDetector();

        GD_Sensor = new Ultrasonic_Sensor();
        GD_MotionDetector = new Ultrasonic_MotionDetector();

        SF_Power = new Power_Component();
        STV_Power = new Power_Component();
        SL_Power = new Power_Component();
        SMW_Power = new Power_Component();
        SGD_Power = new Power_Component();
        MotionDetector_Power = new Power_Component();

        SF_Temperature = new DeviceTemp_Component();
        STV_Temperature = new DeviceTemp_Component();
        SL_Temperature = new DeviceTemp_Component();
        SMW_Temperature = new DeviceTemp_Component();
        SGD_Temperature = new DeviceTemp_Component();

        SF_WiFi = new Network_Component();
        STV_WiFi = new Network_Component();
        SL_WiFi = new Network_Component();
        SMW_WiFi = new Network_Component();
        SGD_WiFi = new Network_Component();
        HUB_WiFi = new Network_Component();


        //SMART HUB TIMER SERVICE
        HUBPowerManager.setTimerService(new ScaledTimeTimerService(1.0));
        SmartHubSystem.setTimerService(new ScaledTimeTimerService(1.0));
        Environment.setTimerService(new ScaledTimeTimerService(1.0));

        //SMART SYSTEM TIMER SERVICE
        SmartFireSystem.setTimerService(new ScaledTimeTimerService(1.0));
        SmartTVSystem.setTimerService(new ScaledTimeTimerService(1.0));
        SmartLightHUB.setTimerService(new ScaledTimeTimerService(1.0));
        SmartMicrowaveSystem.setTimerService(new ScaledTimeTimerService(1.0));
        SmartGarageDoorSystem.setTimerService(new ScaledTimeTimerService(1.0));

        //AUTONOMOUS UNIT TIMER SERVICE
        FireAlarm.setTimerService(new ScaledTimeTimerService(1.0));
        TV.setTimerService(new ScaledTimeTimerService(1.0));
        LED1.setTimerService(new ScaledTimeTimerService(1.0));
        LED2.setTimerService(new ScaledTimeTimerService(1.0));

        Light1.setTimerService(new ScaledTimeTimerService(1.0));
        Light2.setTimerService(new ScaledTimeTimerService(1.0));

        Microwave.setTimerService(new ScaledTimeTimerService(1.0));

        GarageDoor.setTimerService(new ScaledTimeTimerService(1.0));

        //ATOMIC COMPONENTS TIMER SERVICE
        SF_Actuator.setTimerService(new ScaledTimeTimerService(1.0));
        STV_Actuator.setTimerService(new ScaledTimeTimerService(1.0));
        SL_Actuator.setTimerService(new ScaledTimeTimerService(1.0));
        SMW_Actuator.setTimerService(new ScaledTimeTimerService(1.0));
//		SGD_Actuator.setTimerService(new ScaledTimeTimerService(1.0));
        MotionDetector_Actuator.setTimerService(new ScaledTimeTimerService(1.0));

        SF_Controller.setTimerService(new ScaledTimeTimerService(1.0));
        STV_Controller.setTimerService(new ScaledTimeTimerService(1.0));
        SL_Controller.setTimerService(new ScaledTimeTimerService(1.0));
        SMW_Controller.setTimerService(new ScaledTimeTimerService(1.0));
//		SGD_Controller.setTimerService(new ScaledTimeTimerService(1.0));
        MotionDetector_Controller.setTimerService(new ScaledTimeTimerService(1.0));

        SmokeSensor.setTimerService(new ScaledTimeTimerService(1.0));
        FlameSensor.setTimerService(new ScaledTimeTimerService(1.0));
        HeatSensor.setTimerService(new ScaledTimeTimerService(1.0));
        STV_Sensor.setTimerService(new ScaledTimeTimerService(1.0));
        SMW_Sensor.setTimerService(new ScaledTimeTimerService(1.0));
        SGD_Sensor.setTimerService(new ScaledTimeTimerService(1.0));
        US_Sensor.setTimerService(new ScaledTimeTimerService(1.0));
        US_MotionDetector.setTimerService(new ScaledTimeTimerService(1.0));
        GD_Sensor.setTimerService(new ScaledTimeTimerService(1.0));
        GD_MotionDetector.setTimerService(new ScaledTimeTimerService(1.0));

        SF_Power.setTimerService(new ScaledTimeTimerService(1.0));
        STV_Power.setTimerService(new ScaledTimeTimerService(1.0));
        SL_Power.setTimerService(new ScaledTimeTimerService(1.0));
        SMW_Power.setTimerService(new ScaledTimeTimerService(1.0));
        SGD_Power.setTimerService(new ScaledTimeTimerService(1.0));
        MotionDetector_Power.setTimerService(new ScaledTimeTimerService(1.0));

        SF_Temperature.setTimerService(new ScaledTimeTimerService(1.0));
        STV_Temperature.setTimerService(new ScaledTimeTimerService(1.0));
        SL_Temperature.setTimerService(new ScaledTimeTimerService(1.0));
        SMW_Temperature.setTimerService(new ScaledTimeTimerService(1.0));
        SGD_Temperature.setTimerService(new ScaledTimeTimerService(1.0));

        HUB_WiFi.setTimerService(new ScaledTimeTimerService(1.0));
        SF_WiFi.setTimerService(new ScaledTimeTimerService(1.0));
        STV_WiFi.setTimerService(new ScaledTimeTimerService(1.0));
        SL_WiFi.setTimerService(new ScaledTimeTimerService(1.0));
        SMW_WiFi.setTimerService(new ScaledTimeTimerService(1.0));
        SGD_WiFi.setTimerService(new ScaledTimeTimerService(1.0));

        /*
         * THIS SECTION SETS EACH STATECHART VARIABLES TO ITS VALUE
         *
         * WARNING: Without doing this, each variable declaration will default to NULL
         */

        SmartHubSystem.setPowerManager(HUBPowerManager);

        SmartHubSystem.setSF(SmartFireSystem);
        SmartHubSystem.setSTV(SmartTVSystem);
        SmartHubSystem.setSL_HUB(SmartLightHUB);
        SmartHubSystem.setSMW(SmartMicrowaveSystem);
        SmartHubSystem.setSGD(SmartGarageDoorSystem);


        SmartHubSystem.getSL_HUB().setPowerManager(HUBPowerManager);

        //Autonomous Unit Setup
        SmartHubSystem.getSF().setFireAlarm(FireAlarm);
        SmartHubSystem.getSTV().setTV(TV);
        SmartHubSystem.getSL_HUB().setLED1(Light1);
        SmartHubSystem.getSL_HUB().setLED2(Light2);

        SmartHubSystem.getSL_HUB().getLED1().setLights(LED1);
        SmartHubSystem.getSL_HUB().getLED2().setLights(LED2);

        SmartHubSystem.getSMW().setMW(Microwave);

        SmartHubSystem.getSGD().setGarageDoor(GarageDoor);

        //Setting up ACTUATOR Component for each system
//        SmartHubSystem.getSF().setActuator(SF_Actuator);
        SmartHubSystem.getSTV().setActuator(STV_Actuator);
        SmartHubSystem.getSL_HUB().getLED1().setActuator(MotionDetector_Actuator);
        SmartHubSystem.getSL_HUB().getLED2().setActuator(MotionDetector_Actuator);
        SmartHubSystem.getSMW().setActuator(SMW_Actuator);
        SmartHubSystem.getSGD().setActuator(SGD_Actuator);

        //Setting up CONTROLLER Component for each system
//        SmartHubSystem.getSF().setController(SF_Controller);
        SmartHubSystem.getSTV().setController(STV_Controller);
        SmartHubSystem.getSL_HUB().getLED1().setController(MotionDetector_Controller);
        SmartHubSystem.getSL_HUB().getLED2().setController(MotionDetector_Controller);
        SmartHubSystem.getSMW().setController(SMW_Controller);
        SmartHubSystem.getSGD().setController(SGD_Controller);

        //Setting up SENSOR Component for each system
//        SmartHubSystem.getSF().setFlameSensor(FlameSensor);
//        SmartHubSystem.getSF().setSmokeSensor(SmokeSensor);
//        SmartHubSystem.getSF().setHeatSensor(HeatSensor);
        SmartHubSystem.getSTV().setSensor(STV_Sensor);

        SmartHubSystem.getSL_HUB().getLED1().setMotionDetector(US_MotionDetector);
        SmartHubSystem.getSL_HUB().getLED1().getMotionDetector().setActuator(MotionDetector_Actuator);
        SmartHubSystem.getSL_HUB().getLED1().getMotionDetector().setController(MotionDetector_Controller);
        SmartHubSystem.getSL_HUB().getLED1().getMotionDetector().setSensor(US_Sensor);
        SmartHubSystem.getSL_HUB().getLED1().getMotionDetector().setPowerSupply(MotionDetector_Power);


        SmartHubSystem.getSL_HUB().getLED2().setMotionDetector(US_MotionDetector);
        SmartHubSystem.getSL_HUB().getLED2().getMotionDetector().setActuator(MotionDetector_Actuator);
        SmartHubSystem.getSL_HUB().getLED2().getMotionDetector().setController(MotionDetector_Controller);
        SmartHubSystem.getSL_HUB().getLED2().getMotionDetector().setSensor(US_Sensor);
        SmartHubSystem.getSL_HUB().getLED2().getMotionDetector().setPowerSupply(MotionDetector_Power);

        SmartHubSystem.getSMW().setSensor(SMW_Sensor);

        SmartHubSystem.getSGD().setSensor(GD_MotionDetector);
        SmartHubSystem.getSGD().getSensor().setActuator(MotionDetector_Actuator);
        SmartHubSystem.getSGD().getSensor().setController(MotionDetector_Controller);
        SmartHubSystem.getSGD().getSensor().setSensor(GD_Sensor);
        SmartHubSystem.getSGD().getSensor().setPowerSupply(MotionDetector_Power);

        //Setting up POWER Component for each system
        SmartHubSystem.getSF().setPower(SF_Power);
        SmartHubSystem.getSTV().setPower(STV_Power);
        SmartHubSystem.getSL_HUB().getLED1().setPower(SL_Power);
        SmartHubSystem.getSL_HUB().getLED2().setPower(SL_Power);
        SmartHubSystem.getSMW().setPower(SMW_Power);
        SmartHubSystem.getSGD().setPower(SGD_Power);

        //Setting up TEMPERATURE Component for each system
        SmartHubSystem.getSF().setTemp(SF_Temperature);
        SmartHubSystem.getSTV().setTemp(STV_Temperature);
        SmartHubSystem.getSL_HUB().getLED1().setTemp(SL_Temperature);
        SmartHubSystem.getSL_HUB().getLED2().setTemp(SL_Temperature);
        SmartHubSystem.getSMW().setTemp(SMW_Temperature);
        SmartHubSystem.getSGD().setTemp(SGD_Temperature);

        //Setting up WIFI GATEWAY Component for each system
        SmartHubSystem.setWiFi(HUB_WiFi);
        SmartHubSystem.getSF().setWiFi(SF_WiFi);
        SmartHubSystem.getSTV().setWiFi(STV_WiFi);
        SmartHubSystem.getSL_HUB().setWiFi(SL_WiFi);
        SmartHubSystem.getSL_HUB().getLED1().setWiFi(SL_WiFi);
        SmartHubSystem.getSL_HUB().getLED2().setWiFi(SL_WiFi);
        SmartHubSystem.getSMW().setWiFi(SMW_WiFi);
        SmartHubSystem.getSGD().setWiFi(SGD_WiFi);
    }

    //Simulate the statechart
    private int refresh_num = 0;
    protected void run() {

        Simulation.ReadInputFile();


        SmartHubSystem.enter();
        SmartHubSystem.getSL_HUB().enter();

        SmartHubSystem.power().setThreshold(Power_Manager_Threshold);

        SmartHubSystem.getSTV().getSensor().setSimulate_detection_timer(10);
        SmartHubSystem.getSMW().getSensor().setSimulate_detection_timer(10);

        randomTimerValues(SmartHubSystem, r);

        //CONTINUOUS DATA READING FROM THE STATECHARTS
        class refresh extends TimerTask {
            @Override
            public void run() {
                System.out.println(refresh_num++);

                String date_stamp = "("+formatter.format(new Date())+") : SmartHUBSystem V2.0 is running now...";

                if(SmartHubSystem.getWiFi().getConnection()) {
                    LogReport.add("["+formatter.format(new Date())+")]: Application Hub Network Connected...\n");
                }
                else {
                    SmartHubSystem.getWiFi().raiseOn();
//                    LogReport.add("["+formatter.format(new Date())+")]: Application Hub Network Disconnected...\n");
                }

                try {
                    Thread.sleep(1000);
                    readStatechartData(SmartHubSystem); //Refresh Values
                } catch (Exception error) {
//                    System.out.println(error);
                    error.printStackTrace();
                }
            }
        }

        // And From your main() method or any other method
        java.util.Timer timer = new Timer(true);
        TimerTask timerTask = new refresh(); //reference created for TimerTask class

        timer.schedule(timerTask, 0, 999);
        Tester.Handler(SmartHubSystem);
    }

    public static void randomTimerValues(SmartHubSystem SmartHubSystem, Random r) {
        //SMARTFIRE SENSOR TIMEOUTS
        SmartHubSystem.getSF().sensorInput().setSmoke_detection_time(SmokeSensor_detection);
        SmartHubSystem.getSF().sensorInput().setFlame_detection_time(FlameSensor_detection);
        SmartHubSystem.getSF().sensorInput().setHeat_detection_time(HeatSensor_detection);

        SmartHubSystem.getSF().getFireAlarm().sensors().setSmoke_increment(r.nextInt(10 - 1) + 1);
        SmartHubSystem.getSF().getFireAlarm().sensors().setFlame_increment(r.nextInt(10 - 1) + 1);
        SmartHubSystem.getSF().getFireAlarm().sensors().setHeat_increment(r.nextInt(10 - 1) + 1);

        //WIFI TIMEOUTS
        SmartHubSystem.getWiFi().setTimeout_value(r.nextInt(30 - 20) + 20);
        SmartHubSystem.getSF().getWiFi().setTimeout_value(r.nextInt(20 - 5) + 5);
        SmartHubSystem.getSTV().getWiFi().setTimeout_value(r.nextInt(20 - 5) + 5);
        SmartHubSystem.getSMW().getWiFi().setTimeout_value(r.nextInt(20 - 5) + 5);
        SmartHubSystem.getSGD().getWiFi().setTimeout_value(r.nextInt(20 - 5) + 5);
    }

    public static synchronized void playSound(final String AudioFilePath) {
        // The wrapper thread is unnecessary, unless it blocks on the
        // Clip finishing; see comments.
        new Thread(() -> {
            try {
                clip = AudioSystem.getClip();
                inputStream =  AudioSystem.getAudioInputStream(new File(AudioFilePath).getAbsoluteFile());
                clip.open(inputStream);
                clip.start();
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }).start();
    }

    /**
     * SMART HUB DATA
     *
     * THIS FUNCTION ENSURES THAT DATA ON THE YAKINDU STATECHARTS ARE REFLECTED ON TO THE DASHBOARD
     * @throws IOException
     *
     * **/
    public void readStatechartData(SmartHubSystem SmartHubSystem) throws IOException, ExecutionException, InterruptedException {

        //RUNNING FUNCTIONS TO READ DATA FROM STATECHART AND OUTPUT DATA ONTO THE DASHBOARD
        if(SmartHubSystem.getWiFi().getConnection()) {
            readSmartFireSystemDATA(SmartHubSystem);
            readSmartTVSystemDATA(SmartHubSystem);
            readSmartLightSystemDATA(SmartHubSystem);
            readSmartMicrowaveSystemDATA(SmartHubSystem);
            readSmartGarageDoorSystemDATA(SmartHubSystem);
        }

        //Getting Firebase Status
        DocumentSnapshot System_Control_DB = System_Control.get().get();
        Main_Switch_DB = Boolean.TRUE.equals(System_Control_DB.getBoolean("status"));
        SF_status_DB = Boolean.TRUE.equals(System_Control_DB.getBoolean("sys_fire_status"));
        STV_status_DB = Boolean.TRUE.equals(System_Control_DB.getBoolean("sys_tv_status"));
        SL_status_DB = Boolean.TRUE.equals(System_Control_DB.getBoolean("sys_light_status"));
        SMW_status_DB = Boolean.TRUE.equals(System_Control_DB.getBoolean("sys_microwave_status"));
        SGD_status_DB = Boolean.TRUE.equals(System_Control_DB.getBoolean("sys_garage_door_status"));

//        System.out.println(SF_status_DB);

        //Getting Smart System Activity
        allsystem_status = SmartHubSystem.getAllSystemsOn();
        SF_status = SmartHubSystem.isStateActive(smarthub.java.SmartHubSystem.State._SMARTHUBSYSTEM__HUBON_SYSTEM1REGION_SMARTFIRE_STATUS_SMARTFIRESTATUS_ON);
        STV_status = SmartHubSystem.isStateActive(smarthub.java.SmartHubSystem.State._SMARTHUBSYSTEM__HUBON_SMARTHUBREGION_SMARTHUBSYSTEMSTATUS_HUBSTATUSREGION_SYSTEM_MANAGER_SYSTEM1_STV_ON);
        SL_status= SmartHubSystem.isStateActive(smarthub.java.SmartHubSystem.State._SMARTHUBSYSTEM__HUBON_SMARTHUBREGION_SMARTHUBSYSTEMSTATUS_HUBSTATUSREGION_SYSTEM_MANAGER_SYSTEM2_SL_ON);
        SMW_status = SmartHubSystem.isStateActive(smarthub.java.SmartHubSystem.State._SMARTHUBSYSTEM__HUBON_SMARTHUBREGION_SMARTHUBSYSTEMSTATUS_HUBSTATUSREGION_SYSTEM_MANAGER_SYSTEM3_SMW_ON);
        SGD_status = SmartHubSystem.isStateActive(smarthub.java.SmartHubSystem.State._SMARTHUBSYSTEM__HUBON_SMARTHUBREGION_SMARTHUBSYSTEMSTATUS_HUBSTATUSREGION_SYSTEM_MANAGER_SYSTEM4_SGD_ON);

        //Firebase - Sync SmartHub
        if (SmartHubSystem.isStateActive(smarthub.java.SmartHubSystem.State._SMARTHUBSYSTEM__HUBON) != Main_Switch_DB){
            SmartHubSystem.raiseToggle();

            if (!Main_Switch_DB){
                System_Control.update("sys_fire_status", false);
                SmartFireSystemDB.update("status_system", false);
            } else {
                System_Control.update("sys_fire_status", true);
                SmartFireSystemDB.update("status_system", true);
            }
            System_Control.update("sys_tv_status", false);
            System_Control.update("sys_light_status", false);
            System_Control.update("sys_microwave_status", false);
            System_Control.update("sys_garage_door_status", false);

        } else{
            System_Control.update("sys_fire_status", Main_Switch_DB);
            SmartFireSystemDB.update("status_system", Main_Switch_DB);
            if (STV_status != STV_status_DB){
                SmartHubSystem.smartTV().raiseToggle();
            }
            if (SL_status != SL_status_DB){
                SmartHubSystem.smartLightsHUB().raiseToggle();
            }
            if (SMW_status != SMW_status_DB){
                SmartHubSystem.smartMicrowave().raiseToggle();
            }
            if (SGD_status != SGD_status_DB){
                SmartHubSystem.smartGarageDoor().raiseToggle();
            }
        }

        boolean hubState = SmartHubSystem.isStateActive(smarthub.java.SmartHubSystem.State._SMARTHUBSYSTEM__HUBON);

        //POWER MANAGER
        total_HUB_power = (double)(SmartHubSystem.power().getTotal())/(double)(SmartHubSystem.getPowerManager().getThreshold())*100;

        current_Power = (double)SmartHubSystem.power().getTotal();

//        DataPlotter.PowerConsumptionDataFromStatechart(current_Power, SF_Power_kWh, STV_Power_kWh, SMW_Power_kWh, SGD_Power_kWh, SL_Power_kWh);

//        DataPlotter.DialDataset.setValue(total_HUB_power);

        //CHECKS IF ALL SYSTEMS ARE ON
        totalSystemsON = (int)SmartHubSystem.getTotalSystemsON();

        int HUBNetwork_timeout = (int)SmartHubSystem.getWiFi().getTimeout_counter(); //CONNECTION LEVEL

        if(SmartHubSystem.getWiFi().getConnection()) {
            SmartHubSystem.getSF().system().raiseConnect_network();
            SmartHubSystem.getSTV().system().raiseConnect_network();
            SmartHubSystem.getSMW().system().raiseConnect_network();
            SmartHubSystem.getSGD().system().raiseConnect_network();
        }
        else if(!SmartHubSystem.getWiFi().getConnection()){
            SmartHubSystem.getSF().system().raiseDisconnect_network();
            SmartHubSystem.getSTV().system().raiseDisconnect_network();
            SmartHubSystem.getSL_HUB().getLED1().system().raiseDisconnect_network();
            SmartHubSystem.getSL_HUB().getLED2().system().raiseDisconnect_network();
            SmartHubSystem.getSMW().system().raiseDisconnect_network();
            SmartHubSystem.getSGD().system().raiseDisconnect_network();
        }
    }

    /**
     SMART FIRE SYSTEM DATA MANAGEMENT PANEL
     * @throws IOException
     * **/

    public void readSmartFireSystemDATA(SmartHubSystem SmartHubSystem) throws IOException {
        //FOR AUDIO
        try {
            clip = AudioSystem.getClip();
            inputStream =  AudioSystem.getAudioInputStream(new File("Audio/firealarm2.wav").getAbsoluteFile());
            clip.open(inputStream);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        //Generating Random Values
        long 	sensor1 = new Random().nextInt(100),
                sensor2 = new Random().nextInt(100),
                sensor3 = new Random().nextInt(100);

        Statecharts_Initializer.SensorLog.add("(Flame,Smoke,Heat): ("+sensor1+","+sensor2+","+sensor3+")\n");

        ReportLogSaver.SaveSensorData(SensorLog);

//        DataPlotter.sensor1_val = sensor1;
//        DataPlotter.sensor2_val = sensor2;
//        DataPlotter.sensor3_val = sensor3;
//
//        //Setting values onto the bar graph
//        DataPlotter.dataset.setValue(DataPlotter.sensor1_val, DataPlotter.sensor1, "Flame");
//        DataPlotter.dataset.setValue(DataPlotter.sensor2_val, DataPlotter.sensor2, "Smoke");
//        DataPlotter.dataset.setValue(DataPlotter.sensor3_val, DataPlotter.sensor3, "Heat");
//
//        DataPlotter.ChangeColour(DataPlotter.sensor1_val, DataPlotter.sensor2_val, DataPlotter.sensor3_val);

        //Firebase - Update FireAlarm Sensor Data
        SmartFireSystemDB.update("Flame", sensor1);
        SmartFireSystemDB.update("Smoke", sensor2);
        SmartFireSystemDB.update("Heat", sensor3);

        SmartHubSystem.getSF().getFireAlarm().sensors().setFlame_threshold(100);
        SmartHubSystem.getSF().getFireAlarm().sensors().setSmoke_threshold(100);

//        fireAlarm_status = SmartHubSystem.getSF().getFireAlarm().alarm().getSound();
        fireAlarm_sensors_triggered = SmartHubSystem.getSF().getFireAlarm().sensors().getTriggerSignal_received();
        //SAFE or WARNING or DANGER MODE
        String mode_status = SmartHubSystem.getSF().getFireAlarm().message().getStatus();

        //POWER COMPONENT
        SmartHubSystem.smartFire().setKWh(SF_Power_kWh);
        SmartHubSystem.getSF().getPower().setHour(1);
        String SF_kWh = " " + SmartHubSystem.getSF().getPower_input();

        //WIFI COMPONENT
        boolean WiFi_connection = SmartHubSystem.getSF().getWiFi_connection();
        int network_timeout = (int)SmartHubSystem.getSF().getWiFi().getTimeout_counter(); //CONNECTION LEVEL
        String connection = "";
        if(WiFi_connection) {
            connection = "Connected";
        }else {
            connection = "Not Connected";
        }

        //PANEL TITLE
//        DataPlotter.smartFirePanel.setBorder(
//                BorderFactory.createTitledBorder("SYSTEM 1: Smart Fire System ("+connection+" - "+network_timeout+") - "+SF_kWh+" kWh"+ " - (Status: "+mode_status+")"));

        //FIRE ALARM SOUNDING STATUS
        if(fireAlarm_status) {
            SmartHubSystem.hUB().raiseTurnOFFSystems();
        }
        else {
            if(SmartHubSystem.getSF().getFireAlarm().getWarned()){
                //smartFire_message.setBackground(Color.orange.darker());
            }
            //else
            //smartFire_message.setBackground(Color.GREEN.brighter());

            //smartFirePanel.setBackground(Color.WHITE.brighter());
            //smartFire_switch.setText("Fire Alarm: OFF");
        }
    }

    /**
     SMART TV SYSTEM DATA MANAGEMENT PANEL
     * **/

    public void readSmartTVSystemDATA(SmartHubSystem SmartHubSystem) {

        TV_status = SmartHubSystem.isStateActive(smarthub.java.SmartHubSystem.State._SMARTHUBSYSTEM__HUBON_SMARTHUBREGION_SMARTHUBSYSTEMSTATUS_HUBSTATUSREGION_SYSTEM_MANAGER_SYSTEM1_STV_ON);
        TV_usage = SmartHubSystem.getSTV().getTV().device().getIsOn();
        String TV_input = SmartHubSystem.getSTV().getTV().input().getSource();
        String usage_status = "", system_status = "";

        //POWER COMPONENT
        SmartHubSystem.smartTV().setKWh(STV_Power_kWh);
        SmartHubSystem.getSTV().getPower().setHour(1);
        String STV_kWh = " " + SmartHubSystem.smartTV().getKWh();


        //WIFI COMPONENT
        boolean WiFi_connection = SmartHubSystem.getSTV().getWiFi_connection();
        int network_timeout = (int)SmartHubSystem.getSTV().getWiFi().getTimeout_counter(); //CONNECTION TIMEOUT
        String connection = "";
        if(WiFi_connection) {
            connection = "Connected";
        }else {
            connection = "Not Connected";
        }
    }

    /**
     SMART LIGHTS SYSTEM DATA MANAGEMENT PANEL
     * **/

    public void readSmartLightSystemDATA(SmartHubSystem SmartHubSystem) {

        Light_status = SmartHubSystem.smartLightsHUB().getOn();
        Light_usage = SmartHubSystem.getSL_HUB().getInUSE();
        String usage_status = "", system_status = "";
        //Lights Usage Status
        if(Light_status) {
            if(Light_usage)
                usage_status = "Master Switch: ON";
            else usage_status = "Master Switch: OFF";

            system_status = "Light System Status: ON";
        }else {
            system_status = "Light System Status: OFF";
        }

        //POWER COMPONENT
        SmartHubSystem.smartLightsHUB().setKWh(SL_Power_kWh);
        String SL_kWh = " " + SmartHubSystem.smartLightsHUB().getKWh();

        //WIFI COMPONENT
        boolean WiFi_connection = SmartHubSystem.getSL_HUB().network().getConnection();
        int network_timeout = (int)SmartHubSystem.getSL_HUB().getWiFi().getTimeout_counter(); //CONNECTION TIMEOUT
        String connection = "";
        if(WiFi_connection) {
            connection = "Connected";
        }else {
            connection = "Not Connected";
        }
    }

    /**
     SMART MICROWAVE SYSTEM DATA MANAGEMENT PANEL
     * **/

    public void readSmartMicrowaveSystemDATA(SmartHubSystem SmartHubSystem) {
        Microwave_status = SmartHubSystem.isStateActive(smarthub.java.SmartHubSystem.State._SMARTHUBSYSTEM__HUBON_SYSTEM4REGION_SMARTMICROWAVE_STATUS_SMARTMICROWAVESTATUS_ON);

        Microwave_usage = SmartHubSystem.getSMW().getMW().getIn_use();

        boolean food_inside = SmartHubSystem.getSMW().getMW().getFood_inside_sensed();
        boolean doorClosed = SmartHubSystem.getSMW().getMW().door().getClosed();

        String usage_status = "", system_status = "";

        //Lights Usage Status
        if(Microwave_status) {
            if(food_inside) {
                usage_status = "Microwave has food inside";
                if(SmartHubSystem.getSMW().getMW().getIn_use()) {
                    usage_status = SmartHubSystem.getSMW().getMW().getMessage();
                }

            }
            else usage_status =  "Microwave has NO food inside!";

            system_status = "Microwave System Status: ON";
        }else {
            system_status = "Microwave System Status: OFF";

        }

        //POWER COMPONENT
        SmartHubSystem.smartMicrowave().setKWh(SMW_Power_kWh);
        SmartHubSystem.getSMW().getPower().setHour(1);
        String SMW_kWh = " " + SmartHubSystem.smartMicrowave().getKWh();

        //WIFI COMPONENT
        boolean WiFi_connection = SmartHubSystem.getSMW().getWiFi_connection();
        int network_timeout = (int)SmartHubSystem.getSMW().getWiFi().getTimeout_counter(); //CONNECTION TIMEOUT
        String connection = "";
        if(WiFi_connection) {
            connection = "Connected";
        }else {
            connection = "Not Connected";
        }
    }

    /**
     SMART GARAGE DOOR SYSTEM DATA MANAGEMENT PANEL
     * **/

    public void readSmartGarageDoorSystemDATA(SmartHubSystem SmartHubSystem) throws ExecutionException, InterruptedException {
        GarageDoor_status = SmartHubSystem.isStateActive(smarthub.java.SmartHubSystem.State._SMARTHUBSYSTEM__HUBON_SYSTEM5REGION_SMARTGARAGEDOOR_STATUS_SMARTGARAGEDOORSTATUS_ON);
        GarageDoor_usage = !SmartHubSystem.getSGD().getGarageDoor().door().getDoor_closed();
        GarageDoor_block = SmartHubSystem.getSGD().getGarageDoor().door().getBlock();

        //Firebase - Info
        firebase_door_blocked = Boolean.TRUE.equals(SmartGarageDoorSystemDB.get().get().getBoolean("door_blocked"));
        firebase_status_door = Boolean.TRUE.equals(SmartGarageDoorSystemDB.get().get().getBoolean("status_door"));
        firebase_garage_door_sync = Boolean.TRUE.equals(SmartGarageDoorSystemDB.get().get().getBoolean("sync"));

        String usage_status = SmartHubSystem.getSGD().getGarageDoor().system().getDoor_status();
        String system_status = "";

        //Firebase - Sync Garage Door Detail
        if (firebase_garage_door_sync) {
            if (GarageDoor_block != firebase_door_blocked) {
                GarageDoor_block = firebase_door_blocked;
                if (GarageDoor_block) {
                    SmartHubSystem.getSGD().getSensor().getSensor().setEnvironmentData(5);
                } else {
                    SmartHubSystem.getSGD().getSensor().getSensor().setEnvironmentData(0);

                    if (GarageDoor_usage != firebase_status_door) {
                        SmartHubSystem.getSGD().raiseActive_controller();
                    }
                }
            }
            SmartGarageDoorSystemDB.update("sync", false);
        } else {
            //Firebase - update Garage Door - Door Block
            SmartGarageDoorSystemDB.update("door_blocked", GarageDoor_block);
        }

        //Firebase - update Garage Door - Door Status
        SmartGarageDoorSystemDB.update("status_door", GarageDoor_usage);

        //Garage Door Usage Status
        if(GarageDoor_status) {
            system_status = "Garage Door System Status: ON";
        }else {
            system_status = "Garage Door System Status: OFF";
        }

        //POWER COMPONENT
        SmartHubSystem.smartGarageDoor().setKWh(SGD_Power_kWh);
        SmartHubSystem.getSGD().getPower().setHour(1);
        String SGD_kWh = " " + SmartHubSystem.smartGarageDoor().getKWh();


        //WIFI COMPONENT
        boolean WiFi_connection = SmartHubSystem.getSGD().getWiFi_connection();
        int network_timeout = (int)SmartHubSystem.getSGD().getWiFi().getTimeout_counter(); //CONNECTION TIMEOUT
        String connection = "";
        if(WiFi_connection) {
            connection = "Connected";
        }else {
            connection = "Not Connected";
        }
    }
}
