package smarthub.ui;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.yakindu.core.*;
//import com.yakindu.core.rx.Observable;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
//import java.math.*;
import java.text.DecimalFormat;

//FOR AUDIO

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

//FOR UI
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
//import java.awt.event.WindowEvent;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JFrame;

import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultValueDataset;

//Atomic Components
//Smart System Java Code Imports
import smarthub.core.*;
import smarthub.java.*;
import smarthub.java.SmartHubSystem.*;


public class Statecharts_Initializer extends DashboardFrame_Editor {
    private Firestore db;
    private DocumentReference System_Control,
            SmartFireSystemDB, SmartTVSystemDB, SmartLightSystemDB, SmartMicrowaveSystemDB, SmartGarageDoorSystemDB;

    private boolean sys_fire_status = true;

    private static final long serialVersionUID = -8909693541678814631L;
    public static String AudioFilePath;
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

    protected boolean fireAlarm_status, fireAlarm_sensors_triggered, alarmAudio_Played = false,
            TV_status, TV_usage,
            Light_status, Light_usage,
            Microwave_status, Microwave_usage,
            GarageDoor_status, GarageDoor_usage, GarageDoor_block,
            Main_Switch_DB, SF_status_DB, STV_status_DB, SL_status_DB, SMW_status_DB, SGD_status_DB,
            firebase_fire_alarm_snooze,
            firebase_door_blocked, firebase_status_door, firebase_garage_door_sync,
            firebase_TV_status_system, firebase_TV_usage_tv,
            firebase_light_status_system, firebase_light_master_switch;

    protected String firebase_TV_tv_cable;
    protected Long light_brightness, firebase_light_brightness;

    public static ArrayList<String> LogReport = new ArrayList<String>(),
            SensorLog = new  ArrayList<String>();


    public static SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    public static Date date = new Date();

    //Statecharts
    public static SmartHubSystem SmartHubSystem;
    public static SmartFireSystem SmartFireSystem;
    public static SmartTVSystem SmartTVSystem;
    public static SmartLightHUB SmartLightHUB;
    public static SmartMicrowaveSystem SmartMicrowaveSystem;
    public static SmartGarageDoorSystem SmartGarageDoorSystem;

    protected static Environment Environment;

    //Atomic Components used in the Smart Systems
    protected static Actuator_Component STV_Actuator, SL_Actuator, SMW_Actuator, MotionDetector_Actuator;
    protected static Actuator_Component_2 SF_Actuator, SGD_Actuator;
    protected static Controller_Component SF_Controller, STV_Controller, SL_Controller, SMW_Controller, MotionDetector_Controller;
    protected static Controller_Component_2 SGD_Controller;
    protected static Sensor_Component STV_Sensor, SMW_Sensor, SGD_Sensor;
    protected static Sensor_Component_Fire FlameSensor, HeatSensor, SmokeSensor;
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
    protected Simulation simulation;


    public static Statecharts_Initializer application = new Statecharts_Initializer();

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

        db = FirestoreClient.getFirestore();
        System_Control = db.collection("HubSystem").document("System_Control");
        SmartFireSystemDB = db.collection("HubSystem").document("SmartFireSystem");
        SmartTVSystemDB = db.collection("HubSystem").document("SmartTVSystem");
        SmartLightSystemDB = db.collection("HubSystem").document("SmartLightSystem");
        SmartMicrowaveSystemDB = db.collection("HubSystem").document("SmartMicrowaveSystem");
        SmartGarageDoorSystemDB = db.collection("HubSystem").document("SmartGarageDoorSystem");
    }

    public void runStatecharts() throws IOException {
        LogReport.add("["+formatter.format(new Date())+")]: Application Initializing...\n");
        application.init();
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
        SF_Actuator = new Actuator_Component_2();
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

        SmokeSensor = new Sensor_Component_Fire();
        FlameSensor = new Sensor_Component_Fire();
        HeatSensor = new Sensor_Component_Fire();
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

        // THIS SECTION SETS EACH STATECHART VARIABLES TO ITS VALUE
        // WARNING: Without doing this, each variable declaration will default to NULL

        SmartHubSystem.setPowerManager(HUBPowerManager);

        // SmartHubSystem.setSF_Environment(Environment);
        // SmartHubSystem.setSTV_Environment(Environment);
        // SmartHubSystem.setSL_Environment(Environment);
        // SmartHubSystem.setSMW_Environment(Environment);

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
        SmartHubSystem.getSF().setActuator(SF_Actuator);
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
        SmartHubSystem.getSF().setFlameSensor(FlameSensor);
        SmartHubSystem.getSF().setSmokeSensor(SmokeSensor);
        SmartHubSystem.getSF().setHeatSensor(HeatSensor);
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

        //
        setState(JFrame.EXIT_ON_CLOSE);
        //new DashboardFrame_Editor().createContents();
    };

    //Simulate the statechart
    protected void run() {

        Simulation.ReadInputFile();
        //Simulation.SendInputFileToStatechart(SmartHubSystem);

        //smartFirePanel.setVisible(false);
        smartTVPanel.setVisible(false);
        smartLightPanel.setVisible(false);
        smartMicrowavePanel.setVisible(false);
        smartGarageDoorPanel.setVisible(false);

        SmartHubSystem.enter();
        SmartHubSystem.getSL_HUB().enter();
        //System.out.println("Smart Hub System statechart is currently running (?): "+SmartHubSystem.isActive());
        //System.out.println("Power Manager System statechart is currently running (?): "+PowerManager.isActive());
        //System.out.println("Smart Fire System statechart is currently running (?): "+SmartFireSystem.isActive());
        //System.out.println("Smart TV System statechart is currently running (?): "+SmartTVSystem.isActive());
        //System.out.println("Smart Lights System statechart is currently running (?): "+SmartLightSystem.isActive());
        //System.out.println("Smart Microwave System statechart is currently running (?): "+SmartMicrowaveSystem.isActive());


        SmartHubSystem.power().setThreshold(Power_Manager_Threshold);

        SmartHubSystem.getSTV().getSensor().setSimulate_detection_timer(10);
        //SmartHubSystem.getSL_HUB().getLED1().getMotionDetector().setEnvironmentData(10);
        //SmartHubSystem.getSL_HUB().getLED2().getMotionDetector().setEnvironmentData(10);
        SmartHubSystem.getSMW().getSensor().setSimulate_detection_timer(10);


        randomTimerValues(SmartHubSystem, r);


        //CONTINUOUS DATA READING FROM THE STATECHARTS
        class refresh extends TimerTask {
            @Override
            public void run() {

                String date_stamp = "("+formatter.format(new Date())+") : SmartHUBSystem V2.0 is running now...";
                exitPanelText.setText(date_stamp);

                if(SmartHubSystem.getWiFi().getConnection()) {
                    exitPanelText3.setText("Smart Hub System Network Connection: Connected...");
                    LogReport.add("["+formatter.format(new Date())+")]: Application Hub Network Connected...\n");
                }
                else {
                    exitPanelText3.setText("Smart Hub System Network Connection: Disconnected...Please Reconnect...");
                    LogReport.add("["+formatter.format(new Date())+")]: Application Hub Network Disconnected...\n");
                }

                try {
                    Thread.sleep(1000);

//		             IP_Catcher.runtimePing();
//		             IP_Catcher.Local();
                    readStatechartData(SmartHubSystem); //Refresh Values
                } catch (Exception error) {
                    System.out.println(error);
                }
            }
        }

        // And From your main() method or any other method
        Timer timer = new Timer(true);
        TimerTask timerTask = new refresh(); //reference created for TimerTask class

        timer.schedule(timerTask, 0, 999);

        //timer.cancel();

        //BUTTON HANDLERS
        ButtonClicksHandler(SmartHubSystem);
        LogReport.add("["+formatter.format(new Date())+")]: Button Listeners Ready...\n");
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

        //SmartHubSystem.getSL_HUB().getLED1().getMotionDetector().setSensorData(r.nextInt(1,10));

        //WIFI TIMEOUTS
        SmartHubSystem.getWiFi().setTimeout_value(r.nextInt(30 - 20) + 20);
        SmartHubSystem.getSF().getWiFi().setTimeout_value(r.nextInt(20 - 5) + 5);
        SmartHubSystem.getSTV().getWiFi().setTimeout_value(r.nextInt(20 - 5) + 5);
        //SmartHubSystem.getSL_HUB().getWiFi().setTimeout_value(r.nextInt(5,20));
        SmartHubSystem.getSMW().getWiFi().setTimeout_value(r.nextInt(20 - 5) + 5);
        SmartHubSystem.getSGD().getWiFi().setTimeout_value(r.nextInt(20 - 5) + 5);
    }

    public static synchronized void playSound(final String AudioFilePath) {
        new Thread(new Runnable() {
            // The wrapper thread is unnecessary, unless it blocks on the
            // Clip finishing; see comments.
            public void run() {
                try {
                    clip = AudioSystem.getClip();
                    inputStream =  AudioSystem.getAudioInputStream(new File(AudioFilePath).getAbsoluteFile());
                    clip.open(inputStream);
                    clip.start();
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        }).start();
    }
    public static synchronized void stopSound(final String AudioFilePath) {
        new Thread(new Runnable() {
            // The wrapper thread is unnecessary, unless it blocks on the
            // Clip finishing; see comments.
            public void run() {
                try {
                    clip = AudioSystem.getClip();
                    inputStream =  AudioSystem.getAudioInputStream(new File(AudioFilePath).getAbsoluteFile());
                    clip.open(inputStream);
                    clip.stop();
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
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
            readNotificationBarDATA(SmartHubSystem);
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
        SF_status = SmartHubSystem.isStateActive(State._SMARTHUBSYSTEM__HUBON_SYSTEM1REGION_SMARTFIRE_STATUS_SMARTFIRESTATUS_ON);
        STV_status = SmartHubSystem.isStateActive(State._SMARTHUBSYSTEM__HUBON_SMARTHUBREGION_SMARTHUBSYSTEMSTATUS_HUBSTATUSREGION_SYSTEM_MANAGER_SYSTEM1_STV_ON);
        SL_status= SmartHubSystem.isStateActive(State._SMARTHUBSYSTEM__HUBON_SMARTHUBREGION_SMARTHUBSYSTEMSTATUS_HUBSTATUSREGION_SYSTEM_MANAGER_SYSTEM2_SL_ON);
        SMW_status = SmartHubSystem.isStateActive(State._SMARTHUBSYSTEM__HUBON_SMARTHUBREGION_SMARTHUBSYSTEMSTATUS_HUBSTATUSREGION_SYSTEM_MANAGER_SYSTEM3_SMW_ON);
        SGD_status = SmartHubSystem.isStateActive(State._SMARTHUBSYSTEM__HUBON_SMARTHUBREGION_SMARTHUBSYSTEMSTATUS_HUBSTATUSREGION_SYSTEM_MANAGER_SYSTEM4_SGD_ON);

        //Firebase - Sync SmartHub
        if (SmartHubSystem.isStateActive(State._SMARTHUBSYSTEM__HUBON) != Main_Switch_DB){
            SmartHubSystem.raiseToggle();
            sys_fire_status = true;

            if (!Main_Switch_DB){
                System_Control.update("sys_fire_status", false);
                SmartFireSystemDB.update("status_system", false);
            } else {
                System_Control.update("sys_fire_status", true);
                SmartFireSystemDB.update("status_system", true);
                if (sys_fire_status) {

                }
            }
            System_Control.update("sys_tv_status", false);
            System_Control.update("sys_light_status", false);
            System_Control.update("sys_microwave_status", false);
            System_Control.update("sys_garage_door_status", false);

        } else{
            if (sys_fire_status) {
                System_Control.update("sys_fire_status", Main_Switch_DB);
                SmartFireSystemDB.update("status_system", Main_Switch_DB);
            }
            else if (!Main_Switch_DB){
                System_Control.update("sys_fire_status", false);
                SmartFireSystemDB.update("status_system", false);
            }

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

        boolean hubState = SmartHubSystem.isStateActive(State._SMARTHUBSYSTEM__HUBON),
                emergencyState = SmartHubSystem.isStateActive(State._SMARTHUBSYSTEM__HUBON_SMARTHUBREGION_SMARTHUBSYSTEMSTATUS_HUBSTATUSREGION_EMERGENCY_STATE);

        //EMERGENCY STATE HANDLER
        if(emergencyState) {
            buttons[4].setText("EMERGENCY STATE IS ACTIVE");
            smartFire_switch.setText("Fire Alarm: ON");
            smartFire_systemStatus.setText("System Status: Emergency Mode! 911 has been notified.");
        }

        //POWER MANAGER

        total_HUB_power = (double)(SmartHubSystem.power().getTotal())/(double)(SmartHubSystem.getPowerManager().getThreshold())*100;

        current_Power = (double)SmartHubSystem.power().getTotal();

        DataPlotter.PowerConsumptionDataFromStatechart(current_Power, SF_Power_kWh, STV_Power_kWh, SMW_Power_kWh, SGD_Power_kWh, SL_Power_kWh);

        DataPlotter.DialDataset.setValue(total_HUB_power);

        if(total_powerInfo_btn==0) {
            buttons[0].setText("Consumption Level Percentage: "+df.format(total_HUB_power) + " % (MAX: "+SmartHubSystem.getPowerManager().getThreshold()+" kWh)");
        }
        else {
            buttons[0].setText("Current Total Power Consumption: "+df.format(SmartHubSystem.power().getTotal()) + " kWh (MAX: "+SmartHubSystem.getPowerManager().getThreshold()+" kWh)");
        }

        //MAIN SMART HUB STATE
        if(hubState) {
            buttons[1].setText("HUB Status: ON");
            buttons[0].setEnabled(true);
            buttons[2].setEnabled(true);
            buttons[4].setEnabled(true);
            //smartFirePanel.setVisible(true);
            smartTVPanel.setVisible(true);
            smartLightPanel.setVisible(true);
            smartMicrowavePanel.setVisible(true);
            smartGarageDoorPanel.setVisible(true);
        }
        else {
            buttons[1].setText("HUB Status: OFF");
            buttons[0].setEnabled(false);
            buttons[0].setText("Data Unavailable");
            buttons[2].setEnabled(false);
            buttons[4].setEnabled(false);
            buttons[4].setText("");
            //smartFirePanel.setVisible(false);
            smartTVPanel.setVisible(false);
            smartLightPanel.setVisible(false);
            smartMicrowavePanel.setVisible(false);
            smartGarageDoorPanel.setVisible(false);
        }
        //CHECKS IF ALL SYSTEMS ARE ON
        totalSystemsON = (int)SmartHubSystem.getTotalSystemsON();

        if(totalSystemsON>=4) {
            buttons[2].setText("Turn OFF all systems");
        }
        else {
            buttons[2].setText("Turn ON all systems");
        }
        int HUBNetwork_timeout = (int)SmartHubSystem.getWiFi().getTimeout_counter(); //CONNECTION LEVEL

        //SmartHubSystem.getWiFi().setConnection(true);

        //System.out.println(SmartHubSystem.getWiFi().getTimeout_counter()+','+SmartHubSystem.getWiFi().getTimeout_value());

        if(SmartHubSystem.getWiFi().getConnection()) {
            buttons[3].setText("HUB Connected: (Timeout:"+HUBNetwork_timeout+")");
            SmartHubSystem.getSF().system().raiseConnect_network();
            SmartHubSystem.getSTV().system().raiseConnect_network();
            //SmartHubSystem.getSL_HUB().getWiFi().raiseOn();
            SmartHubSystem.getSMW().system().raiseConnect_network();
            SmartHubSystem.getSGD().system().raiseConnect_network();

            buttons[4].setEnabled(true);
            notifsViewButton.setText(notifsView_Title[notifsView_value]);
            notifsViewButton.setEnabled(true);

            //smartFire_switch.setEnabled(true);
            smartTV_switch.setEnabled(true);
            smartTV_use.setEnabled(true);
            smartTV_inputSource.setEnabled(true);

            smartLight_switch.setEnabled(true);
            smartLight_use.setEnabled(true);
            smartLight_brightness.setEnabled(true);

            smartMicrowave_switch.setEnabled(true);
            smartMicrowave_use.setEnabled(true);
            smartMicrowave_startTimer.setEnabled(true);
            smartMicrowave_addTimer.setEnabled(true);
            smartMicrowave_resetTimer.setEnabled(true);
            smartMicrowave_doorStatus.setEnabled(true);

            smartGarageDoor_switch.setEnabled(true);
            smartGarageDoor_use.setEnabled(true);
            smartGarageDoor_block.setEnabled(true);
        }
        else if(!SmartHubSystem.getWiFi().getConnection()){
            buttons[3].setText("Network: Not Connected");
            SmartHubSystem.getSF().system().raiseDisconnect_network();
            SmartHubSystem.getSTV().system().raiseDisconnect_network();
            SmartHubSystem.getSL_HUB().getLED1().system().raiseDisconnect_network();
            SmartHubSystem.getSL_HUB().getLED2().system().raiseDisconnect_network();
            SmartHubSystem.getSMW().system().raiseDisconnect_network();
            SmartHubSystem.getSGD().system().raiseDisconnect_network();
            buttons[4].setText("Network ERROR! Please wait until system reconnects...");
            buttons[4].setEnabled(false);

            notifsViewButton.setText("Network ERROR! Please wait until system reconnects...");
            notifsViewButton.setEnabled(false);
            notifsText1.setText("");
            notifsText2.setText("");
            notifsText3.setText("");
            notifsText4.setText("");

            //smartFirePanel.setBorder(
            //BorderFactory.createTitledBorder("SYSTEM 1 is disconnected"));
            //smartFire_switch.setEnabled(false);
            smartTVPanel.setBorder(BorderFactory.createTitledBorder("SYSTEM 2 is disconnected"));
            smartTV_switch.setEnabled(false);
            smartTV_use.setEnabled(false);
            smartTV_inputSource.setEnabled(false);
            smartLightPanel.setBorder(BorderFactory.createTitledBorder("SYSTEM 3 is disconnected"));
            smartLight_switch.setEnabled(false);
            smartLight_use.setEnabled(false);
            smartLight_brightness.setEnabled(false);
            smartMicrowavePanel.setBorder(BorderFactory.createTitledBorder("SYSTEM 4 is disconnected"));
            smartMicrowave_switch.setEnabled(false);
            smartMicrowave_use.setEnabled(false);
            smartMicrowave_startTimer.setEnabled(false);
            smartMicrowave_addTimer.setEnabled(false);
            smartMicrowave_resetTimer.setEnabled(false);
            smartMicrowave_doorStatus.setEnabled(false);
            smartGarageDoorPanel.setBorder(BorderFactory.createTitledBorder("SYSTEM 5 is disconnected"));
            smartGarageDoor_switch.setEnabled(false);
            smartGarageDoor_use.setEnabled(false);
            smartGarageDoor_block.setEnabled(false);
        }
    }

    /**
     * FUNCTION FOR THE NOTIFICATION TABS
     * **/

    public void readNotificationBarDATA(SmartHubSystem SmartHubSystem) {


        //SYSTEM USAGE
        if(notifsView_value==0){
            //SMART FIRE
            if(SF_status) {
                notifsText1.setText("Smart Fire System: ON");
            }else {
                notifsText1.setText("Smart Fire System: OFF");
            }
            //SMART TV
            if(STV_status) {
                notifsText2.setText("Smart TV System: ON");
            }else {
                notifsText2.setText("Smart TV System: OFF");
            }
            //SMART LIGHTS
            if(SL_status) {
                notifsText3.setText("Smart Lights System: ON");
            }else {
                notifsText3.setText("Smart Lights System: OFF");
            }
            //SMART MICROWAVE
            if(SMW_status) {
                notifsText4.setText("Smart Microwave System: ON");
            }else {
                notifsText4.setText("Smart Microwave System: OFF");
            }
            if(SGD_status) {
                notifsText5.setText("Smart Garage Door System: ON");
            }else {
                notifsText5.setText("Smart Garage Door System: OFF");
            }
        }
        //WIFI STATUS
        else if(notifsView_value==1) {
            //SMARTFIRE
            boolean SF_connection = SmartHubSystem.network().getSF_connection();
            String connection1 = "";
            if(SF_connection) {
                connection1 = "Connected";
                //smartFire_switch.setEnabled(true);
            }else {
                connection1 = "Not Connected...(Reconnecting...)";
                //smartFire_switch.setEnabled(false);
            }
            //STV
            boolean STV_connection = SmartHubSystem.network().getSTV_connection();
            String connection2 = "";
            if(STV_connection) {
                connection2 = "Connected";
                smartTV_switch.setEnabled(true);
                smartTV_use.setEnabled(true);
                smartTV_inputSource.setEnabled(true);
            }else {
                connection2 = "Not Connected...(Reconnecting...)";
                smartTV_switch.setEnabled(false);
                smartTV_use.setEnabled(false);
                smartTV_inputSource.setEnabled(false);
            }
            //SMARTLIGHT
            boolean SL_connection = SmartHubSystem.network().getSL_connection();
            String connection3 = "";
            if(SL_connection) {
                connection3 = "Connected";
                smartLight_switch.setEnabled(true);
            }else {
                connection3 = "Not Connected...(Reconnecting...)";
                smartLight_switch.setEnabled(false);
            }
            //SMARTMICROWAVE
            boolean SMW_connection = SmartHubSystem.network().getSMW_connection();
            String connection4 = "";
            if(SMW_connection) {
                connection4 = "Connected";
                smartMicrowave_switch.setEnabled(true);
            }else {
                connection4 = "Not Connected...(Reconnecting...)";
                smartMicrowave_switch.setEnabled(false);
            }
            //SMARTGARAGEDOOR
            boolean SGD_connection = SmartHubSystem.network().getSGD_connection();
            String connection5 = "";
            if(SGD_connection) {
                connection5 = "Connected";
                smartGarageDoor_switch.setEnabled(true);
                smartGarageDoor_use.setEnabled(true);
                smartGarageDoor_block.setEnabled(true);
            }else {
                connection5 = "Not Connected...(Reconnecting...)";
                smartGarageDoor_switch.setEnabled(false);
                smartGarageDoor_use.setEnabled(false);
                smartGarageDoor_block.setEnabled(false);
            }
            notifsText1.setText("System 1 Network Connection Status: "+connection1);
            notifsText2.setText("System 2 Network Connection Status: "+connection2);
            notifsText3.setText("System 3 Network Connection Status: "+connection3);
            notifsText4.setText("System 4 Network Connection Status: "+connection4);
            notifsText5.setText("System 5 Network Connection Status: "+connection5);
        }
        //USAGE STATUS
        else if(notifsView_value==2) {
            if(fireAlarm_status)
                notifsText1.setText("SYSTEM 1: IN USE");
            else
                notifsText1.setText("SYSTEM 1: NOT IN USE");

            if(TV_usage)
                notifsText2.setText("SYSTEM 2: IN USE ("+smartTV_inputSource.getText()+")");
            else
                notifsText2.setText("SYSTEM 2: NOT IN USE");

            if(Light_usage)
                notifsText3.setText("SYSTEM 3: IN USE");
            else
                notifsText3.setText("SYSTEM 3: NOT IN USE");

            if(Microwave_usage)
                notifsText4.setText("SYSTEM 4: IN USE");
            else
                notifsText4.setText("SYSTEM 4: NOT IN USE");

            if(GarageDoor_usage)
                notifsText5.setText("SYSTEM 5: IN USE");
            else
                notifsText5.setText("SYSTEM 5: NOT IN USE");
        }
        //POWER CONSUMPTION
        else if(notifsView_value==3) {
            double current_Power = (double)SmartHubSystem.power().getTotal();

            String 	SF_power = df.format(((double)SF_Power_kWh/(double)current_Power)*100)+" %",
                    STV_power = df.format(((double)STV_Power_kWh/(double)current_Power)*100)+" %",
                    SL_power = df.format(((double)SL_Power_kWh/(double)current_Power)*100)+" %",
                    SMW_power = df.format(((double)SMW_Power_kWh/(double)current_Power)*100)+" %",
                    SGD_power = df.format(((double)SGD_Power_kWh/(double)current_Power)*100)+" %";


            if(SF_status)
                notifsText1.setText("Smart Fire System: " + SF_power);
            else
                notifsText1.setText("Smart Fire System: 0 %");

            if(TV_status)
                notifsText2.setText("Smart TV System: " + STV_power);
            else
                notifsText2.setText("Smart TV System: " + " 0 %");

            if(Light_status)
                notifsText3.setText("Smart Lights System:  "+ SL_power);
            else
                notifsText3.setText("Smart Lights System: " +"0 %");

            if(Microwave_status)
                notifsText4.setText("Smart Microwave System: " + SMW_power);
            else
                notifsText4.setText("Smart Microwave System: " + "0 %");

            if(GarageDoor_status)
                notifsText5.setText("Smart Garage Door System: " + SGD_power);
            else
                notifsText5.setText("Smart Garage Door System: " + "0 %");
        }
    }

    /**
     SMART FIRE SYSTEM DATA MANAGEMENT PANEL
     * @throws IOException
     * **/

    public void readSmartFireSystemDATA(SmartHubSystem SmartHubSystem) throws IOException, ExecutionException, InterruptedException {
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

//        long 	sensor1 = 200,
//                sensor2 = 60,
//                sensor3 = 50;

        Statecharts_Initializer.SensorLog.add("(Flame,Smoke,Heat): ("+sensor1+","+sensor2+","+sensor3+")\n");

        ReportLogSaver.SaveSensorData(SensorLog);

        DataPlotter.sensor1_val = sensor1;
        DataPlotter.sensor2_val = sensor2;
        DataPlotter.sensor3_val = sensor3;

        //Setting values onto the bar graph
        DataPlotter.dataset.setValue(DataPlotter.sensor1_val, DataPlotter.sensor1, "Flame");
        DataPlotter.dataset.setValue(DataPlotter.sensor2_val, DataPlotter.sensor2, "Smoke");
        DataPlotter.dataset.setValue(DataPlotter.sensor3_val, DataPlotter.sensor3, "Heat");

        DataPlotter.ChangeColour(DataPlotter.sensor1_val, DataPlotter.sensor2_val, DataPlotter.sensor3_val);

//        //Firebase - Update FireAlarm Sensor Data
//        SmartFireSystemDB.update("Flame", sensor1);
//        SmartFireSystemDB.update("Smoke", sensor2);
//        SmartFireSystemDB.update("Heat", sensor3);
        smartFire_switch.setText("Manually Turn OFF");
        smartFire_snooze.setText("Pause");

        SmartHubSystem.getSF().getFlameSensor().setEnvironmentData(sensor1);
        SmartHubSystem.getSF().getSmokeSensor().setEnvironmentData(sensor2);
        SmartHubSystem.getSF().getHeatSensor().setEnvironmentData(sensor3);

//        SmartHubSystem.getSF().getFireAlarm().sensors().setFlame_threshold(90);
//        SmartHubSystem.getSF().getFireAlarm().sensors().setSmoke_threshold(90);
//        SmartHubSystem.getSF().getFireAlarm().sensors().setHeat_threshold(90);

        //SAFE or WARNING or DANGER MODE
        String mode_status = SmartHubSystem.getSF().getFireAlarm().message().getStatus();

//        long test1 = SmartHubSystem.getSF().getFlameSensor().getSensorData();
//        System.out.println(test1);
//
//        fireAlarm_status = SmartHubSystem.getSF().getActuator().getIsTriggered();
//        System.out.println(fireAlarm_status);
        if (sensor1 > 95 || sensor2 > 95 || sensor3 > 95) {
            clip.start();
        }
        else {
            clip.stop();
        }

        firebase_fire_alarm_snooze = Boolean.TRUE.equals(SmartFireSystemDB.get().get().getBoolean("pause"));
        if (firebase_fire_alarm_snooze) {
            SmartFireSystemDB.update("pause", false);
            SmartHubSystem.getSF().getFireAlarm().mode().raisePause();
        }
        //smartFire_message.setText("Current Status: "+mode_status);



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
        }else if(!WiFi_connection) {
            connection = "Not Connected";
        }

        //PANEL TITLE
        DataPlotter.smartFirePanel.setBorder(
                BorderFactory.createTitledBorder("SYSTEM 1: Smart Fire System ("+connection+" - "+network_timeout+") - "+SF_kWh+" kWh"+ " - (Status: "+mode_status+")"));


        //FIRE ALARM SOUNDING STATUS
        if(fireAlarm_status) {
            smartFire_message.setBackground(Color.RED);
            buttons[4].setText("Smart Fire System has an emergency alert!");
            smartFire_switch.setText("Fire Alarm: ON");
            SmartHubSystem.hUB().raiseTurnOFFSystems();
        }
        else {
            if(SmartHubSystem.getSF().getFireAlarm().getWarned()){
                smartFire_message.setBackground(Color.orange.darker());
            }
            //else
            //smartFire_message.setBackground(Color.GREEN.brighter());

            //smartFirePanel.setBackground(Color.WHITE.brighter());
            //smartFire_switch.setText("Fire Alarm: OFF");
        }

        //FIRE ALARM SENSORS TRIGGERED

		/*if(fireAlarm_sensors_triggered) {
			boolean smoke_presence = SmartHubSystem.getSF().getFireAlarm().sensors().getSmoke_presence(),
					flame_presence = SmartHubSystem.getSF().getFireAlarm().sensors().getFlame_presence(),
					heat_presence = SmartHubSystem.getSF().getFireAlarm().sensors().getHeat_presence(),

					smoke_activity = SmartHubSystem.getSF().smokeSensorOutput().getActivity(),
					flame_activity = SmartHubSystem.getSF().flameSensorOutput().getActivity(),
					heat_activity = SmartHubSystem.getSF().heatSensorOutput().getActivity();

			String system_detection_status = SmartHubSystem.getSF().sensorOutput().getDetected();

			System.out.println("PRESENCE(Smoke, Flame, Heat) :("+smoke_presence+flame_presence+heat_presence+")");
			System.out.println("ACTIVITY(Smoke, Flame, Heat) :("+smoke_activity+flame_activity+heat_activity+")");

			if(smoke_presence) {
				smartFire_systemStatus.setText("System Status: "+system_detection_status+" (Timer: "+SmartHubSystem.getSF().getFireAlarm().timer().getCounter()+")");
			}
			else if(flame_presence) {
				smartFire_systemStatus.setText("System Status: "+system_detection_status+" (Timer: "+SmartHubSystem.getSF().getFireAlarm().timer().getCounter()+")");
			}
			else if(heat_presence) {
				smartFire_systemStatus.setText("System Status: "+system_detection_status+" (Timer: "+SmartHubSystem.getSF().getFireAlarm().timer().getCounter()+")");
			}

	        //CARBON AND SMOKE LEVELS
			String flame_value, smoke_value, heat_value;

			//SMOKE LEVEL
	        if(SmartHubSystem.getSF().getFireAlarm().sensors().getSmoke_value()>=100) {
	        	SmartHubSystem.getSF().getFireAlarm().mode().raiseDanger();
		        smoke_value = "MAX 100";
	        }
	        else {
		        smoke_value = " "+ SmartHubSystem.getSF().getFireAlarm().sensors().getSmoke_value();
	        }

	        //CARBON LEVEL
	        if(SmartHubSystem.getSF().getFireAlarm().sensors().getFlame_value()>=100) {
	        	SmartHubSystem.getSF().getFireAlarm().mode().raiseDanger();
	        	flame_value = "MAX 100";
	        }
	        else {
	        	flame_value = " "+ SmartHubSystem.getSF().getFireAlarm().sensors().getFlame_value();
	        }

	        //HEAT LEVEL
	        if(SmartHubSystem.getSF().getFireAlarm().sensors().getHeat_value()>=100) {
	        	SmartHubSystem.getSF().getFireAlarm().mode().raiseDanger();
	        	heat_value = "MAX 100";
	        }
	        else {
	        	heat_value = " "+ SmartHubSystem.getSF().getFireAlarm().sensors().getHeat_value();
	        }

	        smartFire_smokeSensor.setText("Smoke Sensor Level (Obscuration / ft): "+ smoke_value +"% over threshold");
	        smartFire_flameSensor.setText("Flame Sensor Level (Parts per million): "+flame_value+"% over threshold");
	        smartFire_heatSensor.setText("Heat Sensor Level (Celsius): "+ heat_value +"% over threshold");
		}
		else {
			smartFire_systemStatus.setText("System Status: Monitoring...");

	        //CARBON, SMOKE , HEAT LEVELS
	        smartFire_smokeSensor.setText("Smoke Sensor Level (Obscuration / ft): Monitoring...");
	        smartFire_flameSensor.setText("Flame Sensor Level (Parts per million): Monitoring...");
	        smartFire_heatSensor.setText("Heat Sensor Level (Celsius): Monitoring...");
		}*/
    }


    /**
     SMART TV SYSTEM DATA MANAGEMENT PANEL
     * **/

    public void readSmartTVSystemDATA(SmartHubSystem SmartHubSystem) throws ExecutionException, InterruptedException {

        TV_status = SmartHubSystem.isStateActive(State._SMARTHUBSYSTEM__HUBON_SMARTHUBREGION_SMARTHUBSYSTEMSTATUS_HUBSTATUSREGION_SYSTEM_MANAGER_SYSTEM1_STV_ON);
        TV_usage = SmartHubSystem.getSTV().getTV().device().getIsOn();
        String TV_input = SmartHubSystem.getSTV().getTV().input().getSource();
        String usage_status = "", system_status = "";

        firebase_TV_status_system = Boolean.TRUE.equals(SmartTVSystemDB.get().get().getBoolean("status_system"));
        firebase_TV_usage_tv = Boolean.TRUE.equals(SmartTVSystemDB.get().get().getBoolean("usage_tv"));
        firebase_TV_tv_cable = SmartTVSystemDB.get().get().getString("tv_cable");

        if(TV_status != firebase_TV_status_system) {
            SmartHubSystem.smartTV().raiseToggle();
        }
        if (firebase_TV_status_system) {
            if(TV_usage != firebase_TV_usage_tv) {
                if (firebase_TV_usage_tv){
                    SmartHubSystem.getSTV().getTV().device().raiseOn();
                }
                else {
                    SmartHubSystem.getSTV().getTV().device().raiseOff();
                }
            }

            if (firebase_TV_usage_tv){
                assert firebase_TV_tv_cable != null;
                switch (firebase_TV_tv_cable) {
                    case "2131886306":
                        SmartHubSystem.getSTV().getTV().input().raiseCable();
                        break;
                    case "2131886307":
                        SmartHubSystem.getSTV().getTV().input().raiseSatellite();
                        break;
                    case "2131886301":
                        SmartHubSystem.getSTV().getTV().input().raiseHdmi();
                        break;
                }
            }
        }

        TV_status = SmartHubSystem.isStateActive(State._SMARTHUBSYSTEM__HUBON_SMARTHUBREGION_SMARTHUBSYSTEMSTATUS_HUBSTATUSREGION_SYSTEM_MANAGER_SYSTEM1_STV_ON);
        TV_usage = SmartHubSystem.getSTV().getTV().device().getIsOn();
        TV_input = SmartHubSystem.getSTV().getTV().input().getSource();


//        if (firebase_TV_status_system) {
//            SmartTVSystemDB.addSnapshotListener((value, error) -> {
//                if (error != null) {
//                    LogReport.add("Listen failed: " + error.getMessage());
//                    return;
//                }
//
//                assert value != null;
//                Map<String, Object> task = value.getData();
//                assert task != null;
//                firebase_TV_usage_tv = (Boolean) Objects.requireNonNull(task.get("usage_tv"));
//                firebase_TV_tv_cable = (String) Objects.requireNonNull(task.get("tv_cable"));
//
//            });
//        }

        //TV Usage Status
        if(TV_status) {
            if(TV_usage) {
                usage_status = "TV Usage Status: IN USE";
                smartTV_inputSource.setEnabled(true);
                smartTV_inputSource.setText(TV_input);
            }
            else {
                usage_status = "TV Usage Status: NOT IN USE";
                smartTV_inputSource.setEnabled(false);
                smartTV_inputSource.setText("Data Currently Not Available");
            }

            system_status = "TV System Status: ON";

            smartTV_use.setEnabled(true);
            smartTV_use.setText(usage_status);
            smartTVPanel.setBackground(Color.green);
        }else {
            system_status = "TV System Status: OFF";
            smartTVPanel.setBackground(Color.gray.brighter());
            smartTV_use.setEnabled(false);
            smartTV_inputSource.setEnabled(false);
            smartTV_inputSource.setText("Data Currently Not Available");
        }

        smartTV_switch.setText(system_status);

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
            smartTV_use.setEnabled(true);
            smartTV_inputSource.setEnabled(true);
        }else {
            connection = "Not Connected";
            smartTV_use.setEnabled(false);
            smartTV_inputSource.setEnabled(false);
        }

        //PANEL TITLE
        smartTVPanel.setBorder(
                BorderFactory.createTitledBorder("SYSTEM 2: Smart TV System ("+connection+" - "+network_timeout+") - "+STV_kWh+" kWh"));

    }

    /**
     SMART LIGHTS SYSTEM DATA MANAGEMENT PANEL
     * **/

    public void readSmartLightSystemDATA(SmartHubSystem SmartHubSystem) throws ExecutionException, InterruptedException, IOException {
//        BufferedReader br = new BufferedReader(new FileReader("~/"));
//        try {
//            String line = br.readLine();
//            String lastLine = "";
//
//            while (line != null) {
//                lastLine = line;
//            }
//            System.out.println(lastLine);
//        } finally {
//            br.close();
//        }

        Light_status = SmartHubSystem.smartLightsHUB().getOn();
        Light_usage = SmartHubSystem.getSL_HUB().getInUSE();
        String usage_status = "", system_status = "";

        firebase_light_status_system = Boolean.TRUE.equals(SmartLightSystemDB.get().get().getBoolean("status_system"));;
        firebase_light_master_switch = Boolean.TRUE.equals(SmartLightSystemDB.get().get().getBoolean("status_light"));;
        try {
            firebase_light_brightness = SmartLightSystemDB.get().get().getLong("brightness_level");
        }
        catch (ClassCastException ignored) {
            Double pad = SmartLightSystemDB.get().get().getDouble("brightness_level");
            assert pad != null;
            firebase_light_brightness = pad.longValue();
        }

        if(Light_status != firebase_light_status_system) {
            SmartHubSystem.smartLightsHUB().raiseToggle();
        }
        if (firebase_light_status_system) {
            if(Light_usage != firebase_light_master_switch) {
                if(firebase_light_master_switch) {
                    SmartHubSystem.getSL_HUB().hUB().raiseTurnONSystems();
                    smartLight_brightness.setEnabled(true);
                }
                else {
                    SmartHubSystem.getSL_HUB().hUB().raiseTurnOFFSystems();
                    smartLight_brightness.setEnabled(false);
                }
                smartLight_brightness.setText("Brightness Level: " + firebase_light_brightness);
            }
        }

        Light_status = SmartHubSystem.smartLightsHUB().getOn();
        Light_usage = SmartHubSystem.getSL_HUB().getInUSE();

        //Lights Usage Status
        if(Light_status) {
            if(Light_usage)
                usage_status = "Master Switch: ON";
            else usage_status = "Master Switch: OFF";

            system_status = "Light System Status: ON";

            smartLight_use.setEnabled(true);
            smartLight_use.setText(usage_status);
            smartLightPanel.setBackground(Color.green);

			/*var brightness_level = SmartHubSystem.getSL().getLights().brightness().getLevel();
			smartLight_brightness.setText("Brightness Level: "+brightness_level);
			smartLight_brightness.setEnabled(true);

			if(brightness_level==1) {
				smartLightPanel.setBackground(Color.orange.darker());
			}else if(brightness_level==2) {
				smartLightPanel.setBackground(Color.orange);
			}else if(brightness_level==3) {
				smartLightPanel.setBackground(Color.yellow.darker());
			}else if(brightness_level==4) {
				smartLightPanel.setBackground(Color.yellow.brighter());
			}*/

        }else {
            system_status = "Light System Status: OFF";
            smartLightPanel.setBackground(Color.gray.brighter());
            smartLight_use.setEnabled(false);
            smartLight_use.setText("Data Currently Not Available");
            smartLight_brightness.setEnabled(false);
        }

        smartLight_switch.setText(system_status);

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

        //PANEL TITLE
        smartLightPanel.setBorder(
                BorderFactory.createTitledBorder("SYSTEM 3: Smart Lights System ("+connection+" - "+network_timeout+") - "+SL_kWh+" kWh"));

    }

    /**
     SMART MICROWAVE SYSTEM DATA MANAGEMENT PANEL
     * **/

    public void readSmartMicrowaveSystemDATA(SmartHubSystem SmartHubSystem) {


        Microwave_status = SmartHubSystem.isStateActive(State._SMARTHUBSYSTEM__HUBON_SYSTEM4REGION_SMARTMICROWAVE_STATUS_SMARTMICROWAVESTATUS_ON);

        Microwave_usage = SmartHubSystem.getSMW().getMW().getIn_use();

        boolean food_inside = SmartHubSystem.getSMW().getMW().getFood_inside_sensed();
        boolean doorClosed = SmartHubSystem.getSMW().getMW().door().getClosed();

        String usage_status = "", system_status = "";

        //Lights Usage Status
        if(Microwave_status) {
            smartMicrowave_addTimer.setVisible(true);
            smartMicrowave_startTimer.setVisible(true);
            smartMicrowave_resetTimer.setVisible(true);
            smartMicrowave_doorStatus.setVisible(true);

            smartMicrowave_addTimer.setText("Current timer: +"+SmartHubSystem.getSMW().getMW().device().getTimer()+" seconds...");
            smartMicrowave_startTimer.setText("Start/Stop Timer");
            smartMicrowave_resetTimer.setText("Reset Timer");



            //Disble timer if value is zero
            if(SmartHubSystem.getSMW().getMW().device().getTimer() <= 0) {
                smartMicrowave_startTimer.setEnabled(false);
                smartMicrowave_resetTimer.setEnabled(false);
            }else {
                smartMicrowave_startTimer.setEnabled(true);
                smartMicrowave_resetTimer.setEnabled(true);
            }

            if(doorClosed) {
                smartMicrowave_doorStatus.setText("Microwave Door: CLOSED");
            }else {
                smartMicrowave_startTimer.setEnabled(false);
                smartMicrowave_doorStatus.setText("Microwave Door: OPEN");
            }

            if(food_inside) {
                usage_status = "Microwave has food inside";
                if(SmartHubSystem.getSMW().getMW().getIn_use()) {
                    usage_status = SmartHubSystem.getSMW().getMW().getMessage();
                }
            }
            else usage_status =  "Microwave has NO food inside!";

            system_status = "Microwave System Status: ON";

            smartMicrowave_use.setEnabled(true);
            smartMicrowave_use.setText(usage_status);
            smartMicrowavePanel.setBackground(Color.green);
        }else {
            system_status = "Microwave System Status: OFF";
            smartMicrowavePanel.setBackground(Color.gray.brighter());
            smartMicrowave_use.setEnabled(false);
            smartMicrowave_use.setText("Data Currently Not Available");

            smartMicrowave_addTimer.setVisible(false);
            smartMicrowave_startTimer.setVisible(false);
            smartMicrowave_resetTimer.setVisible(false);
            smartMicrowave_doorStatus.setVisible(false);
        }

        smartMicrowave_switch.setText(system_status);

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

        //PANEL TITLE
        smartMicrowavePanel.setBorder(
                BorderFactory.createTitledBorder("SYSTEM 4: Smart Microwave System ("+connection+" - "+network_timeout+") - "+SMW_kWh+" kWh"));

    }

    /**
     SMART GARAGE DOOR SYSTEM DATA MANAGEMENT PANEL
     * **/

    public void readSmartGarageDoorSystemDATA(SmartHubSystem SmartHubSystem) throws ExecutionException, InterruptedException {
        GarageDoor_status = SmartHubSystem.isStateActive(State._SMARTHUBSYSTEM__HUBON_SYSTEM5REGION_SMARTGARAGEDOOR_STATUS_SMARTGARAGEDOORSTATUS_ON);
        GarageDoor_usage = !SmartHubSystem.getSGD().getGarageDoor().door().getDoor_closed();
        GarageDoor_block = SmartHubSystem.getSGD().getGarageDoor().door().getBlock();
//        GarageDoor_block = Environment.isStateActive(Environment.State.SIMENVIRONMENT_START_SIMULATION_ULTRASONIC_SENSORS_BLOCK);

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
            smartGarageDoorPanel.setBackground(Color.red);

            if(GarageDoor_usage) {
                smartGarageDoor_block.setEnabled(true);
                if(GarageDoor_block) {
                    smartGarageDoorPanel.setBackground(Color.yellow);
                    smartGarageDoor_block.setText("Garage Door: Blocked.");
                }
                else {
                    smartGarageDoorPanel.setBackground(Color.green);
                    smartGarageDoor_block.setText("Garage Door: Cleared.");
                }
            }
            else {
                smartGarageDoor_block.setEnabled(false);
                smartGarageDoor_block.setText("Data Currently Not Available");
            }

            system_status = "Garage Door System Status: ON";
            smartGarageDoor_use.setEnabled(true);
        }else {
            system_status = "Garage Door System Status: OFF";
            smartGarageDoorPanel.setBackground(Color.gray.brighter());
            smartGarageDoor_use.setEnabled(false);
            smartGarageDoor_block.setEnabled(false);
            smartGarageDoor_block.setText("Data Currently Not Available");
        }

        smartGarageDoor_switch.setText(system_status);
        smartGarageDoor_use.setText(usage_status);

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
            smartGarageDoor_use.setEnabled(true);
            smartGarageDoor_block.setEnabled(true);
        }else {
            connection = "Not Connected";
            smartGarageDoor_use.setEnabled(false);
            smartGarageDoor_block.setEnabled(false);
        }

        //PANEL TITLE
        smartGarageDoorPanel.setBorder(BorderFactory.createTitledBorder
                ("SYSTEM 5: Smart Garage Door System ("+connection+" - "+network_timeout+") - "+SGD_kWh+" kWh"));
    }

    /**
     BUTTON CLICKS HANDLER
     * **/

    public int total_powerInfo_btn = 0;
    //private int[] valuesArrays;

    public void ButtonClicksHandler(final SmartHubSystem SmartHubSystem) {
        //TOTAL POWER CONSUMPTION SWITCH
        buttons[0].addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                anti_freeze = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(total_powerInfo_btn==0) {
                            total_powerInfo_btn = 1;
                        }
                        else {
                            total_powerInfo_btn = 0;
                        }
                    }
                });
                anti_freeze.start();
            }
        });
        //SMART HUB MAIN SWITCH
        buttons[1].addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                anti_freeze = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        playSound("Audio/click.wav");
                        SmartHubSystem.raiseToggle();
                        LogReport.add("["+formatter.format(new Date())+")]: HUB Switch Toggled !\n");
                        sys_fire_status = true;

                        //Firebase - update main switch
                        System_Control.update("status", SmartHubSystem.isStateActive(State._SMARTHUBSYSTEM__HUBON));
//                        System.out.println(Main_Switch_DB);
                        if (!SmartHubSystem.isStateActive(State._SMARTHUBSYSTEM__HUBON)){
                            System_Control.update("sys_fire_status", false);
                            System_Control.update("sys_tv_status", false);
                            System_Control.update("sys_light_status", false);
                            System_Control.update("sys_microwave_status", false);
                            System_Control.update("sys_garage_door_status", false);
                            SmartFireSystemDB.update("status_system", false);
                            SmartTVSystemDB.update("status_system", false);
                            SmartLightSystemDB.update("status_system", false);
                            SmartMicrowaveSystemDB.update("status_system", false);
                            SmartGarageDoorSystemDB.update("status_system", false);
                        }
                    }
                });
                anti_freeze.start();
            }
        });

        //TURNING ON AND OFF ALL SYSTEMS
        buttons[2].addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {

                anti_freeze = new Thread(new Runnable() {

                    @Override
                    public void run() {// TODO Auto-generated method stub
                        totalSystemsON = (int)SmartHubSystem.getTotalSystemsON();
                        playSound("Audio/click.wav");
                        if(totalSystemsON>=5) {
                            SmartHubSystem.hUB().raiseTurnOFFSystems();
                            LogReport.add("["+formatter.format(new Date())+")]: HUB reports POWER OVERLOAD !\n");
                        }
                        else {
                            SmartHubSystem.hUB().raiseTurnONSystems();
                        }
                    }
                });
                anti_freeze.start();
            }

        });
        //HUB WiFi Connection
        buttons[3].addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
                if(SmartHubSystem.getWiFi().getConnection()) {
                    SmartHubSystem.getWiFi().raiseOff();
                    LogReport.add("["+formatter.format(new Date())+")]: HUB WiFi Disconnected! \n");
                    //SmartHubSystem.getWiFi().setConnection(false);
                }
                else if(!SmartHubSystem.getWiFi().getConnection()){
                    SmartHubSystem.getWiFi().raiseOn();
                    LogReport.add("["+formatter.format(new Date())+")]: HUB WiFi Connected! \n");
                    //SmartHubSystem.getWiFi().setConnection(true);
                }
                //System.exit(0);
            }

        });

        //HUB Notification
        buttons[4].addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                playSound("Audio/click.wav");
                buttons[4].setText("WiFi Connection: Connected...");
            }

        });

        notifsViewButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent arg0) {

                anti_freeze = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        playSound("Audio/click.wav");
                        notifsView_value++;
                        if (notifsView_value >= notifsView_Title.length) {
                            notifsView_value = 0;
                        }
                        notifsViewButton.setText(notifsView_Title[notifsView_value]);
                    }
                });
                anti_freeze.start();
            }
        });

        /**
         * TESTER
         */
        //HUB WiFi Connection
        /**  testButton1.addActionListener(new ActionListener(){

        @Override
        public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
        if(SmartHubSystem.getWiFi().getConnection()) {
        SmartHubSystem.getWiFi().raiseOff();
        //SmartHubSystem.getWiFi().setConnection(false);
        }
        else if(!SmartHubSystem.getWiFi().getConnection()){
        SmartHubSystem.getWiFi().raiseOn();
        //SmartHubSystem.getWiFi().setConnection(true);
        }
        //System.exit(0);
        }

        });**/

        /**
         *
         *
         * SMART SYSTEM BUTTONS BELOW
         *
         *
         * **/

        //SMART FIRE SWITCH
        /*smartFire_switch.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				anti_freeze = new Thread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						playSound("Audio/click.wav");
						if(fireAlarm_status) {
							SmartHubSystem.getSF().getFireAlarm().mode().raiseSafe();

							SmartHubSystem.getSF().getSmokeSensor().setSimulate_detection_timer(r.nextInt(15,20));
							SmartHubSystem.getSF().getFlameSensor().setSimulate_detection_timer(r.nextInt(15,20));
							SmartHubSystem.getSF().getHeatSensor().setSimulate_detection_timer(r.nextInt(15,20));

							SmartHubSystem.getSF().getFlameSensor().setActivity(false);
							SmartHubSystem.getSF().getHeatSensor().setActivity(false);
							SmartHubSystem.getSF().getSmokeSensor().setActivity(false);

							SmartHubSystem.getSF().sensorOutput().setActivity(false);

							SmartHubSystem.getSF().sensorInput().raiseReset();

							//SmartHubSystem.getSF().getSmok


							randomTimerValues(SmartHubSystem, r);
					        //smartFire_switch.setText("Fire Alarm: OFF");
						}
						else if(!fireAlarm_status){ //to turn on
							//smartFirePanel.setBackground(Color.RED.brighter());
							SmartHubSystem.getSF().getFireAlarm().mode().raiseDanger();
							SmartHubSystem.getSF().getFlameSensor().setCounter(0);
							SmartHubSystem.getSF().getSmokeSensor().setCounter(0);
							SmartHubSystem.getSF().getHeatSensor().setCounter(0);
					        //smartFire_switch.setText("Fire Alarm: ON");
						}
				}
				});
				anti_freeze.start();
			}
        });*/

        //FIRE SWITCH
        smartFire_switch.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                anti_freeze = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        playSound("Audio/click.wav");
                        SmartHubSystem.getSF().getFireAlarm().mode().raiseManual_off();
                        LogReport.add("["+formatter.format(new Date())+")]: Smart Fire System Switch Toggled !\n");
                        sys_fire_status = false;

                        //Firebase - update Fire Alarm
                        System_Control.update("sys_fire_status", false);
                        SmartFireSystemDB.update("status_system", false);
                    }
                });
                anti_freeze.start();
            }
        });

        //FIRE SNOOZE
        smartFire_snooze.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                anti_freeze = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        playSound("Audio/click.wav");
                        LogReport.add("["+formatter.format(new Date())+")]: Smart Fire System Switch Toggled !\n");

                        SmartFireSystemDB.update("pause", true);
                    }
                });
                anti_freeze.start();
            }
        });

        //TV SWITCH
        smartTV_switch.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                anti_freeze = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        playSound("Audio/click.wav");
                        SmartHubSystem.smartTV().raiseToggle();
                        LogReport.add("["+formatter.format(new Date())+")]: Smart TV System Switch Toggled !\n");

                        //Firebase - update TV
                        System_Control.update("sys_tv_status", SmartHubSystem.isStateActive(State._SMARTHUBSYSTEM__HUBON_SMARTHUBREGION_SMARTHUBSYSTEMSTATUS_HUBSTATUSREGION_SYSTEM_MANAGER_SYSTEM1_STV_ON));
                        SmartTVSystemDB.update("status_system", SmartHubSystem.isStateActive(State._SMARTHUBSYSTEM__HUBON_SMARTHUBREGION_SMARTHUBSYSTEMSTATUS_HUBSTATUSREGION_SYSTEM_MANAGER_SYSTEM1_STV_ON));
                    }
                });
                anti_freeze.start();

            }
        });
        //TV USE
        smartTV_use.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                anti_freeze = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        playSound("Audio/click.wav");
                        if(TV_usage) {
                            SmartHubSystem.getSTV().getTV().device().raiseOff();
                            LogReport.add("["+formatter.format(new Date())+")]: TV not in use...\n");
                        }else {
                            SmartHubSystem.getSTV().getTV().device().raiseOn();
                        }
                        LogReport.add("["+formatter.format(new Date())+")]: TV is in use...\n");
                        SmartTVSystemDB.update("usage_tv", !TV_usage);
                    }
                });
                anti_freeze.start();
            }
        });
        //TV INPUT SOURCE
        // 2131886306 - CABLE
        // 2131886307 - SATELLITE
        // 2131886301 - HDMI
        smartTV_inputSource.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                anti_freeze = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        playSound("Audio/click.wav");
                        if(TV_input_index == 1) {
                            TV_input_index++;
                            SmartHubSystem.getSTV().getTV().input().raiseHdmi();
                            LogReport.add("["+formatter.format(new Date())+")]: TV input changed to HDMI.\n");
                            SmartTVSystemDB.update("tv_cable", "2131886301");
                        }
                        else if(TV_input_index == 2) {
                            TV_input_index++;
                            SmartHubSystem.getSTV().getTV().input().raiseCable();
                            LogReport.add("["+formatter.format(new Date())+")]: TV input changed to CABLE.\n");
                            SmartTVSystemDB.update("tv_cable", "2131886306");
                        }
                        else if(TV_input_index == 3) {
                            TV_input_index = 1;
                            SmartHubSystem.getSTV().getTV().input().raiseSatellite();
                            LogReport.add("["+formatter.format(new Date())+")]: TV input changed to SATELLITE.\n");
                            SmartTVSystemDB.update("tv_cable", "2131886307");
                        }
                    }
                });
                anti_freeze.start();
            }
        });

        //LIGHT SWITCH
        smartLight_switch.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                anti_freeze = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        playSound("Audio/click.wav");

                        boolean systemSwitch = SmartHubSystem.smartLightsHUB().getOn();

                        SmartHubSystem.smartLightsHUB().raiseToggle();
                        if(systemSwitch) {
                            LogReport.add("["+formatter.format(new Date())+")]: Lights are switched off!\n");
                        }
                        else {
                            LogReport.add("["+formatter.format(new Date())+")]: Lights are switch on!\n");
                        }

                        //Firebase - update Light
                        System_Control.update("sys_light_status", SmartHubSystem.isStateActive(State._SMARTHUBSYSTEM__HUBON_SMARTHUBREGION_SMARTHUBSYSTEMSTATUS_HUBSTATUSREGION_SYSTEM_MANAGER_SYSTEM2_SL_ON));
                        SmartLightSystemDB.update("status_system", SmartHubSystem.isStateActive(State._SMARTHUBSYSTEM__HUBON_SMARTHUBREGION_SMARTHUBSYSTEMSTATUS_HUBSTATUSREGION_SYSTEM_MANAGER_SYSTEM2_SL_ON));
                    }
                });
                anti_freeze.start();
            }
        });

        //LED LIGHT UNIT SWITCH
        smartLight_use.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                anti_freeze = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        playSound("Audio/click.wav");
                        boolean masterSwitch = SmartHubSystem.getSL_HUB().getInUSE();

                        if(masterSwitch) {
                            SmartHubSystem.getSL_HUB().hUB().raiseTurnOFFSystems();
                            LogReport.add("["+formatter.format(new Date())+")]: Lights are switched off!\n");
                        }
                        else {
                            SmartHubSystem.getSL_HUB().hUB().raiseTurnONSystems();
                            LogReport.add("["+formatter.format(new Date())+")]: Lights are switch on!\n");
                        }
                        SmartLightSystemDB.update("status_light", !masterSwitch);
                    }
                });
                anti_freeze.start();
            }
        });

        smartLight_brightness.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                anti_freeze = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        playSound("Audio/click.wav");
                        boolean masterSwitch = SmartHubSystem.getSL_HUB().getInUSE();

                        if(masterSwitch) {
                            firebase_light_brightness += 10;
                            if(firebase_light_brightness > 100) {
                                firebase_light_brightness = 0L;
                            }
                            smartLight_brightness.setText("Brightness Level: " + firebase_light_brightness);
                            SmartLightSystemDB.update("brightness_level", firebase_light_brightness);
                        }
                    }
                });
                anti_freeze.start();
            }
        });
//        smartLight_use.addActionListener(e -> {
//            playSound("Audio/click.wav");
//            boolean masterSwitch = SmartHubSystem.getSL_HUB().getInUSE();
//
//            if(masterSwitch) {
//                SmartHubSystem.getSL_HUB().hUB().raiseTurnOFFSystems();
//                LogReport.add("["+formatter.format(new Date())+")]: Lights are switched off!\n");
//            }
//            else {
//                SmartHubSystem.getSL_HUB().hUB().raiseTurnONSystems();
//                LogReport.add("["+formatter.format(new Date())+")]: Lights are switch on!\n");
//            }
//
//        });

        /*
      //LED LIGHT 1 UNIT SWITCH
        smartLight_led1.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				anti_freeze = new Thread(new Runnable() {

					@Override
					public void run() {
						playSound("Audio/click.wav");
						boolean bulbisOn = SmartHubSystem.getSL_HUB().getLED1().getLights().bulb().getIsOn();

						if(bulbisOn) {
							SmartHubSystem.getSL_HUB().getLED1().getMotionDetector().trigger().raiseOff();
						}
						else {
							SmartHubSystem.getSL_HUB().getLED1().getMotionDetector().trigger().raiseOn();
						}
					}
				});
				anti_freeze.start();
			}
        });

       //LED LIGHT 2 UNIT SWITCH
        smartLight_led2.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				anti_freeze = new Thread(new Runnable() {

					@Override
					public void run() {
						playSound("Audio/click.wav");
						boolean bulbisOn = SmartHubSystem.getSL_HUB().getLED1().getLights().bulb().getIsOn();

						if(bulbisOn) {
							SmartHubSystem.getSL_HUB().getLED1().getMotionDetector().trigger().raiseOff();
						}
						else {
							SmartHubSystem.getSL_HUB().getLED1().getMotionDetector().trigger().raiseOn();
						}
					}
				});
				anti_freeze.start();
			}
        });
        */
        /**************   MICROWAVE BUTTONS   ***************/

        //Microwave System Switch
        smartMicrowave_switch.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                anti_freeze = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        playSound("Audio/click.wav");
                        SmartHubSystem.smartMicrowave().raiseToggle();

                        //Firebase - update Microwave
                        System_Control.update("sys_microwave_status", SmartHubSystem.isStateActive(State._SMARTHUBSYSTEM__HUBON_SMARTHUBREGION_SMARTHUBSYSTEMSTATUS_HUBSTATUSREGION_SYSTEM_MANAGER_SYSTEM3_SMW_ON));
                        SmartMicrowaveSystemDB.update("status_system", SmartHubSystem.isStateActive(State._SMARTHUBSYSTEM__HUBON_SMARTHUBREGION_SMARTHUBSYSTEMSTATUS_HUBSTATUSREGION_SYSTEM_MANAGER_SYSTEM3_SMW_ON));
                    }
                });
                anti_freeze.start();
            }

        });
        //Microwave Unit Switch
        smartMicrowave_use.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                anti_freeze = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        playSound("Audio/click.wav");
                        boolean food_inside = SmartHubSystem.getSMW().getMW().getFood_inside_sensed();

                        if(food_inside) {
                            SmartHubSystem.getSMW().getSensor().setActivity(false);
                        }
                        else {
                            SmartHubSystem.getSMW().getSensor().setActivity(true);
                        }
                    }
                });
                anti_freeze.start();
            }
        });
        //Microwave Add Timer Switch
        smartMicrowave_addTimer.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                anti_freeze = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        playSound("Audio/click.wav");
                        SmartHubSystem.getSMW().getMW().device().raiseAddTimer();
                    }
                });
                anti_freeze.start();
            }
        });
        //Microwave Reset Timer Switch
        smartMicrowave_resetTimer.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                anti_freeze = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        playSound("Audio/click.wav");
                        SmartHubSystem.getSMW().getMW().device().raiseResetTimer();
                    }
                });
                anti_freeze.start();
            }
        });
        //Microwave Start Timer Switch
        smartMicrowave_startTimer.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                anti_freeze = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        playSound("Audio/click.wav");
                        SmartHubSystem.getSMW().getMW().device().raiseStart();
                    }
                });
                anti_freeze.start();
            }
        }); //Microwave Door Switch
        smartMicrowave_doorStatus.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                anti_freeze = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        playSound("Audio/click.wav");
                        boolean doorClosed = SmartHubSystem.getSMW().getMW().door().getClosed();

                        if(doorClosed) {
                            SmartHubSystem.getSMW().getMW().door().raiseOpen();
                        }else {
                            SmartHubSystem.getSMW().getMW().door().raiseClose();
                        }
                    }
                });
                anti_freeze.start();
            }
        });

        /**************   GARAGE DOOR BUTTONS   ***************/
        //Garage Door SWITCH
        smartGarageDoor_switch.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                anti_freeze = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        playSound("Audio/click.wav");
                        SmartHubSystem.smartGarageDoor().raiseToggle();
                        LogReport.add("["+formatter.format(new Date())+")]: Smart Garage Door System Switch Toggled !\n");

                        //Firebase - update Garage Door
                        System_Control.update("sys_garage_door_status", SmartHubSystem.isStateActive(State._SMARTHUBSYSTEM__HUBON_SMARTHUBREGION_SMARTHUBSYSTEMSTATUS_HUBSTATUSREGION_SYSTEM_MANAGER_SYSTEM4_SGD_ON));
                        SmartGarageDoorSystemDB.update("status_system", SmartHubSystem.isStateActive(State._SMARTHUBSYSTEM__HUBON_SMARTHUBREGION_SMARTHUBSYSTEMSTATUS_HUBSTATUSREGION_SYSTEM_MANAGER_SYSTEM4_SGD_ON));
                    }
                });
                anti_freeze.start();
            }
        });
        //Garage Door USE
        smartGarageDoor_use.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                anti_freeze = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        playSound("Audio/click.wav");
                        if(GarageDoor_usage) {
                            LogReport.add("["+formatter.format(new Date())+")]: Try to close the door...\n");
                        }else {
                            LogReport.add("[" + formatter.format(new Date()) + ")]: Opening the door...\n");
                        }
                        SmartHubSystem.getSGD().raiseActive_controller();
                    }
                });
                anti_freeze.start();
            }
        });
        //Garage Door INPUT SOURCE
        smartGarageDoor_block.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                anti_freeze = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        playSound("Audio/click.wav");
                        if(GarageDoor_block) {
                            SmartHubSystem.getSGD().getSensor().getSensor().setEnvironmentData(0);
                            LogReport.add("["+formatter.format(new Date())+")]: Remove door block.\n");
                        }else {
                            SmartHubSystem.getSGD().getSensor().getSensor().setEnvironmentData(5);
                            LogReport.add("["+formatter.format(new Date())+")]: Add door block.\n");
                        }

                        //Firebase - update Garage Door - Door Block
                        SmartGarageDoorSystemDB.update("door_blocked", !GarageDoor_block);
                    }
                });
                anti_freeze.start();
            }
        });

        exitButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                LogReport.add("["+formatter.format(new Date())+")]: Application Terminated.\n");
                try {
                    ReportLogSaver.SaveReportLogToFile(LogReport);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                System.exit(0);
            }
        });

        saveReportButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    ReportLogSaver.SaveReportLogToFile(LogReport);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });

        /**
         * FOR BUTTON BEHAVIOURS
         * -->THIS IS TO PREVENT ANY JFRAME CRASHES
         * -->CRASH MAY OCCUR DUE TO MULTIPLE STATECHARTS RUNNING SIMULTANEOUSLY
         *
         * anti_freeze = new Thread(new Runnable() {

        @Override
        public void run() {
        }
        });
         anti_freeze.start();
         *
         * **/
    }


    /**
     * END OF FILE
     *
     * Author: Clyde Rempillo
     * Toronto Metropolitan University
     * Written in January 2023
     * **/
}
