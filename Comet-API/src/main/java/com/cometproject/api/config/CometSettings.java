package com.cometproject.api.config;

import com.cometproject.api.game.rooms.filter.FilterMode;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;

import java.time.DayOfWeek;
import java.util.Map;
import java.util.Set;


public class CometSettings {
    public static boolean motdEnabled = false;
    public static String motdMessage = "";
    public static String hotelName = "";
    public static String hotelUrl = "";
    public static String aboutImg = "";

    public static boolean onlineRewardEnabled = false;
    public static boolean survivalEnabled = true;
    public static int onlineRewardCredits = 0;
    public static int onlineRewardDuckets = 3;

    public static int onlineRewardDiamondsInterval = 45;
    public static int onlineSalaryInterval = 45;
    public static int onlineRewardDiamonds = 0;
    public static int onlineRewardInterval = 15;
    public static Set<DayOfWeek> onlineRewardDoubleDays = Sets.newHashSet();

    public static int groupCost = 0;

    public static boolean aboutShowPlayersOnline = true;
    public static boolean aboutShowUptime = true;
    public static boolean aboutShowRoomsActive = true;

    public static int floorEditorMaxX = 0;
    public static int floorEditorMaxY = 0;
    public static int floorEditorMaxTotal = 0;

    public static int roomMaxPlayers = 150;
    public static boolean roomEncryptPasswords = false;
    public static int roomPasswordEncryptionRounds = 10;
    public static boolean roomCanPlaceItemOnEntity = false;
    public static int roomMaxBots = 15;
    public static int roomMaxPets = 15;
    public static int roomIdleMinutes = 20;

    public static FilterMode wordFilterMode = FilterMode.DEFAULT;

    public static boolean useDatabaseIp = false;
    public static boolean saveLogins = false;

    public static boolean playerInfiniteBalance = false;
    public static int playerGiftCooldown = 30;

    public static final Map<String, String> strictFilterCharacters = Maps.newHashMap();
    public static boolean playerFigureValidation = false;
    public static int playerChangeFigureCooldown = 5;
    public static int callForHelpCooldown = 300;
    public static int calendarTimestamp = 1585699200;

    public static int messengerMaxFriends = 1100;
    public static boolean messengerLogMessages = false;

    public static int cameraPhotoItemId = 50001;
    public static int oreItemId = 3933;
    public static String cameraPhotoUrl = "http://localhost:8080/camera/photo/%photoId%";
    public static String cameraUploadUrl = "http://localhost:8080/camera/upload/%photoId%";
    public static String thumbnailUploadUrl = "http://localhost:8080/camera/upload/%photoId%";
    public static String webSocketUrl = "http://localhost/photos/photos/%photoId%.png";

    public static int roomWiredRewardMinimumRank = 7;
    public static boolean asyncCatalogPurchase = false;

    public static boolean storagePlayerQueueEnabled = false;
    public static boolean storageItemQueueEnabled = false;

    public static boolean adaptiveEntityProcessDelay = false;

    public static int maxConnectionsPerIpAddress = 2;

    public static boolean playerRightsItemPlacement = true;

    public static boolean groupChatEnabled = false;
    public static int survivalMinQueue = 4;
    public static boolean logCatalogPurchases = false;

    public static boolean hallOfFameEnabled = false;
    public static boolean toggleWeenMode = false;
    public static boolean snowStormEnabled = true;
    public static int snowStormMinPlayers = 8;
    public static String hallOfFameCurrency = "";
    public static int hallOfFameRefreshMinutes = 5;
    public static String hallOfFameTextsKey = "";

    public static boolean bonusBagEnabled = false;
    public static String bonusBagConfiguration = "";

    public static int wiredMaxEffects = 10;
    public static int wiredMaxTriggers = 10;
    public static int wiredMaxExecuteStacks = 5;
    public static int betSystemRoomId = 0;
    public static int bankSystemMinimumRequired = 0;
    public static boolean bankSystemSeasonalEnabled = false;
    public static boolean maxConnectionsBlockSuspicious = true;
    public static boolean betSystemEnabled = true;

    public static int maxSeasonalRewardPoints = 0;
    public static int seasonalRewardActivityPoints = 0;
    public static boolean eventWinnerNotification = false;
    public static boolean casinoFreeRolls = false;
    public static boolean console_debugging = false;
    public static int casinoRoomId = 0;

    // Roleplay Assets
    public static int rp_hunger_interval = 0;
    public static int rp_hunger_tick_amount = 0;
    public static int rp_starving_interval = 0;
    public static int rp_starving_tick_amount = 0;

    public static int hospitalRoomId = 0;
    public static int hospitalSalary = 0;
    public static int policeRoomId = 0;
    public static int policeSalary = 0;
    public static int lawRoomId = 0;
    public static int lawSalary = 0;
    public static int mafiaRoomId = 0;
    public static int mafiaSalary = 0;
    public static int politicsRoomId = 0;
    public static int politicsSalary = 0;

    public static int gulagRoomId = 7392;

    public static boolean currencySystemEnabled = false;

    public static int currentEventRoom = 0;
    public static int lotteryPool = 5;
    public static boolean cryptoActive = false;
    public static int communityGoal = 100;

    public static int globalEggsCrafted = 1;
    public static int baseWelcomeRoomId = 9184;
    public static String communityGoalPrize = "ADM";

    private static final Logger log = Logger.getLogger(CometSettings.class.getName());

    /**
     * Enable & set the Message Of The Day text
     *
     * @param motd The message to display to the user on-login
     */
    public static void setMotd(String motd) {
        motdEnabled = true;
        motdMessage = motd;
    }

    public static void setCasinoFreeRolls(boolean b){
        casinoFreeRolls = b;
    }
    public static void setWeenEvent(boolean b){ toggleWeenMode = b; }
    public static void toggleSnowStorm(boolean b){ snowStormEnabled = b; }
    public static void setSurvivalEnabled(boolean b){ survivalEnabled = b; }

    public static int incrementLimitedEgg(){
        globalEggsCrafted++;
        return globalEggsCrafted;
    }

    public int getHospitalRoomId() { return hospitalRoomId; }

    public static void setLotteryPool(int pool){
        lotteryPool = pool;
    }

    public static void setCurrentEventRoom(int r){
        currentEventRoom = r;
    }
    public static void setSurvivalMinQueue(int r){
        survivalMinQueue = r;
    }

    public int getEventID() { return currentEventRoom; }

    public static void toggleCrypto(boolean t) { cryptoActive = t; }
}
