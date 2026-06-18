package com.paly.legend.character;

import com.paly.legend.common.BusinessException;
import com.paly.legend.common.CurrentUser;
import com.paly.legend.config.ClassConfig;
import com.paly.legend.config.GameConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CharacterService {

    private static final String DEFAULT_CLASS = "warrior";

    private final CharacterRepository characterRepository;
    private final GameConfigService gameConfigService;
    private final CharacterStatService characterStatService;

    public CharacterService(CharacterRepository characterRepository,
                            GameConfigService gameConfigService,
                            CharacterStatService characterStatService) {
        this.characterRepository = characterRepository;
        this.gameConfigService = gameConfigService;
        this.characterStatService = characterStatService;
    }

    @Transactional
    public CreateCharacterResponse create(CurrentUser currentUser, CreateCharacterRequest request) {
        if (characterRepository.existsByAccountId(currentUser.getAccountId())) {
            throw new BusinessException("CHARACTER_ALREADY_CREATED", "当前账号已经创建过角色");
        }

        String nickname = normalizeNickname(request.getNickname());
        if (characterRepository.nicknameExists(nickname)) {
            throw new BusinessException("CHARACTER_NICKNAME_EXISTS", "角色昵称已存在");
        }

        String className = normalizeClassName(request.getClassName());
        ClassConfig classConfig = gameConfigService.getClassRequired(className);
        int power = calculatePower(classConfig.getHp(), classConfig.getAttack(), classConfig.getDefense(), classConfig.getAttackSpeed());
        Long characterId = characterRepository.create(
                currentUser.getAccountId(),
                nickname,
                className,
                classConfig.getHp(),
                classConfig.getAttack(),
                classConfig.getDefense(),
                classConfig.getAttackSpeed(),
                power);
        return new CreateCharacterResponse(characterId);
    }

    public CharacterResponse me(CurrentUser currentUser) {
        PlayerCharacter character = characterRepository.findByAccountId(currentUser.getAccountId());
        if (character == null) {
            throw new BusinessException("CHARACTER_NOT_CREATED", "请先创建角色");
        }
        CharacterResponse response = CharacterResponse.from(character);
        ClassConfig classConfig = gameConfigService.getClassRequired(character.getClassName());
        response.setClassDisplayName(classConfig.getName());
        response.setClassDescription(classConfig.getDescription());
        response.setTalentPoints(characterStatService.totalTalentPoints(character));
        response.setUsedTalentPoints(characterStatService.usedTalentPoints(character.getId()));
        response.setAvailableTalentPoints(response.getTalentPoints() - response.getUsedTalentPoints());
        return response;
    }

    private String normalizeNickname(String nickname) {
        return nickname == null ? "" : nickname.trim();
    }

    private String normalizeClassName(String className) {
        String value = className == null ? "" : className.trim().toLowerCase();
        if (value.isEmpty()) {
            return DEFAULT_CLASS;
        }
        gameConfigService.getClassRequired(value);
        return value;
    }

    private int calculatePower(int hp, int attack, int defense, int attackSpeed) {
        return hp + attack * 8 + defense * 6 + attackSpeed * 2;
    }
}
