package com.shoalter.willy.shoaltertools.inventory;

import com.shoalter.willy.shoaltertools.service.CheckMigrationDataService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

@Slf4j
public class CheckMigrationData {
  @Autowired CheckMigrationDataService checkMigrationDataService;
  @Autowired ReactiveRedisTemplate<String, String> redisTempl;

  @Test
  public void checkMigration() {
    List<String> uuidList =
        List.of(
            "3d3eb12e-2ce8-5c63-8812-7e56223d669c",
            "3881d72f-a245-5c35-84ff-a58f471e62f3",
            "bbb7b003-1ec0-50ec-9309-3c1a6247c606",
            "d802b856-f340-5ae5-a6cb-a6adaba882c8",
            "a32aa802-f7e6-564e-af50-8fe936f32eb7",
            "82b88a37-cf53-5b75-a65c-06b3bb27a70e",
            "7ab7ff06-a5e3-5d4e-ac5b-74bcb0b691c0",
            "9bfe8b2b-109f-5e0f-b22f-62379689cb04",
            "aef8f329-a4e9-5f03-82f4-397384c33da8",
            "113263f6-3ff1-5448-bb0a-49c9a1880bdd",
            "993974fe-ff96-5faa-aaf9-d2fd27dcbf81",
            "195b8560-4c52-5d8c-9b9f-3afc4d44b5c7",
            "b66cf6d8-2dca-553c-912c-2e1d4fba02f6",
            "b8886363-b255-5b97-aac1-77a82115981a",
            "c9fc2165-c2dd-581e-98eb-22c7962ab9cd",
            "2f6b7502-1989-5a32-9788-56ca0de36d40",
            "faeaf26a-bd40-57e3-a125-c8067f6c0a04",
            "c3b3bb78-3f35-58e6-89eb-9d9b21565c57",
            "98ceca64-def1-5a2b-8790-e7e723e59bc3",
            "a59d7626-8f32-59a7-b59f-a4511081afd6",
            "d03e6a9c-c3b2-552f-b1bd-95dc2004fb96",
            "9ffd3679-969d-5487-9266-77accb6bb758",
            "81873a70-3d60-54f5-bda8-ef76ab20d5a2",
            "a8f7d486-1699-5d21-bbff-cfb13c25ebb4",
            "29cc4850-3212-5ae6-a0b7-541d0d278c25",
            "86c566c6-0275-50db-a758-cf004281da37",
            "c3f70a6d-812b-5b9a-815b-8d1c751bb8e1",
            "32b28b5b-8a2d-55e8-a895-fb0ec86863be",
            "577fd814-82c3-52fd-aa91-2473723a2865",
            "ae3b932e-94f6-595c-ad8f-e505e2e5132b",
            "0fd52808-8e26-5da7-92c2-3f057b5284e6",
            "8af25c48-2984-530f-827c-55fe1bc63872",
            "129bd2f6-7279-5970-8eea-12b406eb524f",
            "e71ab7fa-9860-5f57-bd81-f2f1d9e35bd6",
            "40d89d00-71da-5dad-9dd2-a6c75ef9ec3b",
            "1fac2b5e-d669-57aa-8c37-36d982bc6ea0",
            "e58dc6d8-8c75-5631-9e49-838bd1e96812",
            "8830e216-db18-53de-a9bf-036aa4b6aacd",
            "526d7518-8925-5a9a-8d5d-e348643289b1",
            "a6d30143-5730-532e-bf0a-f305f3e124c6",
            "4f17dae6-6c0a-5145-8c3f-9faac91e6e7e",
            "972f5f3f-0e97-529c-b8ba-417e9a1b7863",
            "1cccfe82-114f-556f-b045-274c1be831d2",
            "6b42adeb-2566-50b2-a31e-87da1931193c",
            "e54d4edf-08a5-5113-8f37-3b02549c9410",
            "e916009d-edbc-5755-938c-af82a70b00e8",
            "bce3c5d9-4c71-5e34-bdfb-15ded8bdeb18",
            "606a270f-a602-58bc-9658-25b48c3eb7db",
            "64e5e2e5-ebbd-5433-8109-cd47d9ba049c",
            "89bb582e-5708-570e-a1f7-9057348e5c65",
            "e16bd5b6-b056-5619-b914-cc26dbd932b1",
            "5e2f7553-f621-5653-b848-fdc4a7164c93",
            "302124da-c886-503b-a5df-747a7472de52",
            "6de7cfcb-32bf-5d8d-8cae-d68d730ab083",
            "46b6cd7b-e06e-5dbe-9eb4-ec394683f1db",
            "27230fae-20ca-55cc-8e00-55f480ed178c",
            "b0d80dfd-d450-51e3-98b3-fe2be84bf1ea",
            "d311ba29-69e5-5f75-9cef-287a39f6a8e5",
            "50371d8e-f623-5a08-aaf4-fb127373690a",
            "40a3ae14-9962-5520-adb6-245155c37aa6",
            "4e5656b3-75ae-5616-be02-52fff9b01e56",
            "6340c599-676c-5dd3-bca2-a6f3e8b6c5e5",
            "50390c48-dc9c-5885-bf29-d900f55641e9",
            "78dffb57-fdfd-5c7d-90ee-8c1d6ade5190",
            "be1b8474-570c-59fa-818d-cfd1757c7542",
            "588da059-0bae-5450-b5b5-3802e0c0d2ff",
            "00b1cb0c-970a-582b-9a11-1fdf76843652",
            "7d6f643a-1e0a-5f0d-9872-b169cd317fdc",
            "7f75a2aa-b6bb-5499-b195-99a9d4175f5a",
            "2838ca97-61e9-5f79-ac85-790dd60500a6",
            "44405fc5-7188-58af-a89e-7077a4c180e3",
            "7c38ebc9-691b-5897-9f30-37a4562407de",
            "884040dd-119e-5ae8-b14d-0b0e65f25020",
            "950651a1-97f0-56ff-ab2f-52eb2e934613",
            "12b29e50-a174-5e14-be19-1b727b6b4761",
            "ce65ef60-3728-50c3-ab87-2e7ffc337705",
            "c28abe05-ad81-5e51-8f7f-75dea33dacc9",
            "1a548501-2745-5eab-9bf5-344689b0365d",
            "c5abcafe-6c01-522a-b9d0-6f94c0e6807b",
            "ec683984-5590-5637-870e-5a1444f8da76",
            "0ef2d4f1-542d-5908-a2e5-0dbf793f9a63",
            "2376dcfb-0a06-5fe5-80cc-9d6252182055",
            "ab0bba69-932c-5908-bf18-35b7550d0022",
            "02c9b0b7-f8b1-50c1-b05b-377f29e70797",
            "9914ebf5-df0f-5ae9-96c6-1c1802473098",
            "a921bba7-d6ca-566c-b267-a407699145fb",
            "039682ba-0d0d-5f2d-947c-4e6cdbb4c4f8",
            "675333b2-c1c4-58e0-b18e-f4c3b3ef74d8",
            "44d66343-7e25-5c88-9a26-2806e1bacbff",
            "5c5e205f-654e-55cf-9795-c46e27337e41",
            "a6647465-5842-5967-9bda-9d79b0034845",
            "2452aac7-32c8-5ec4-b823-37ef7911e243",
            "db55b64b-513d-5d9b-be3f-fe67f0bc24af",
            "a8f620ba-49b3-50e9-b050-ff4b277ea17b",
            "0fdaf701-fe60-5f14-97b0-5b63b70d92c3",
            "2f7852ea-5ae0-5cba-b817-a49c2e59a9cb",
            "067fbae7-8567-5485-b262-b8c9b7e2d1d2",
            "37c15fb5-0403-5f28-9c03-5644b7484e04",
            "834dc9d3-b9f1-52dd-bb8e-65da0f896e2c",
            "1d0229d2-fcaa-5754-860c-eaea8246541b");
    log.info(
        "=============================start check migration data=============================");
    for (String uuid : uuidList) {
      Map<String, String> oldStockLevelMap =
          redisTempl
              .<String, String>opsForHash()
              .entries(uuid)
              .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
              .block();
      Map<String, String> newStockLevelMap =
          redisTempl
              .<String, String>opsForHash()
              .entries("inventory:" + uuid)
              .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
              .block();
      if (newStockLevelMap.isEmpty()) {
        log.error("uuid[" + uuid + "] doesn't has data");
        continue;
      }
      Map<String, String> iimsStockLevelMap =
          redisTempl
              .<String, String>opsForHash()
              .entries(newStockLevelMap.get("hktv_sku"))
              .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
              .block();
      checkMigrationDataService.checkMigration(
          oldStockLevelMap, newStockLevelMap, iimsStockLevelMap, uuid);
    }
    log.info("=============================end check migration data=============================");
    System.exit(0);
  }
}
