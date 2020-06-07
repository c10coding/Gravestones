package net.dohaw.play.gravestones;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scoreboard.CollisionRules;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.text.Text;

public class GhostTeamMaker {

    private Scoreboard scoreboard = Scoreboard.builder().build();
    private Team ghostTeam = Team.builder().name("GhostTeam").build();

    public GhostTeamMaker(){}

    public void makeScoreboard(){
        ghostTeam.allowFriendlyFire();
        ghostTeam.setCollisionRule(CollisionRules.PUSH_OWN_TEAM);
        ghostTeam.canSeeFriendlyInvisibles();
        ghostTeam.setDisplayName(Text.of("Ghost"));
        scoreboard.registerTeam(ghostTeam);
    }

    public void addPlayerToScoreboard(Player playerToAdd){
        playerToAdd.setScoreboard(scoreboard);
        ghostTeam.addMember(playerToAdd.getTeamRepresentation());
    }


}
