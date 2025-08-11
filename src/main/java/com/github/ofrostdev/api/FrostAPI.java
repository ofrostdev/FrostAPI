package com.github.ofrostdev.api;

import com.github.ofrostdev.api.event.Event;
import com.github.ofrostdev.api.event.dispatch.AsyncEventDispatcher;
import com.github.ofrostdev.api.event.dispatch.SyncEventDispatcher;
import com.github.ofrostdev.api.manager.FrostManager;
import com.github.ofrostdev.api.manager.RegisterFactory;
import com.github.ofrostdev.api.task.TaskController;
import com.github.ofrostdev.api.utils.ActionBar;
import com.github.ofrostdev.api.utils.Config;
import com.github.ofrostdev.api.utils.database.DatabaseFactory;
import com.henryfabio.sqlprovider.connector.SQLConnector;
import org.bukkit.plugin.java.JavaPlugin;

public class FrostAPI extends JavaPlugin {

    private static SQLConnector sqlConnector;

    @Override
    public void onEnable(){
        saveDefaultConfig();

        RegisterFactory.init(this);
        DatabaseFactory factory = new DatabaseFactory(this);
        sqlConnector = factory.createConnector(getConfig().getConfigurationSection("database"));

        getLogger().info("Conexão com o repositório estabelecida com sucesso.");

        FrostManager.init(this);
        Event.init(this);
        AsyncEventDispatcher.init(this);
        SyncEventDispatcher.init(this);
        Config.init(this);
        ActionBar.init(this);
        TaskController.init(this);
    }

    public static SQLConnector getSqlConnector(){
        return sqlConnector;
    }

}
