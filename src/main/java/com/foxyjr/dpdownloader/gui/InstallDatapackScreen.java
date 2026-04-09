package com.foxyjr.dpdownloader.gui;

import com.google.gson.*;
import com.foxyjr.dpdownloader.Mod;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.apache.commons.io.FileUtils;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class InstallDatapackScreen extends Screen {
	public int totalResult = 0;
	private EditBox searchDatapacksField;
	private EditBox searchWorldsField;
	private Button searchButton;
	private Button moreButton;
	private Button backButton;
	private final Screen parent;
	public DatapackWorldListWidget worldList;
	public DatapackListWidget datapackList;
	public DatapackInfoListWidget datapackInfoList;
	private String oldSelectedWorld;
	private String tempPath = "";
	private JsonObject datapackDownloaderData = new JsonObject();
	
	public InstallDatapackScreen(Screen parent) {
		super(Component.translatable("datapackdownloader.title"));
		this.parent = parent;
	}

	public InstallDatapackScreen(Screen parent, String tempPath) {
		super(Component.translatable("datapackdownloader.title"));
		this.parent = parent;
		this.tempPath = tempPath;
	}

	@Override
	public void resize(int width, int height) {
		String oldSearchDatapacksField = this.searchDatapacksField.getValue();
		String oldSearchWorldsField = this.searchWorldsField.getValue();
		List<DatapackInfo> oldDatapackInfo = this.datapackList.getDatapacks();
		this.init(width, height);
		this.searchDatapacksField.setValue(oldSearchDatapacksField);
		this.searchWorldsField.setValue(oldSearchWorldsField);
		this.datapackList.setDatapacks(oldDatapackInfo);
	}

	@Override
	public void onClose() {
        minecraft.setScreen(parent);
	}
	
	@Override
	protected void init() {
		this.searchDatapacksField = new EditBox(this.font, 120 + 12 + 28 + 5, 38, this.width - (28 * 2) - (120 + 12 + 5) - 56, 20, Component.translatable("datapackdownloader.field.search.datapack"));
		this.searchWorldsField = new EditBox(this.font, 28, 38, 132, 20, Component.translatable("datapackdownloader.field.search.world"));
		this.searchWorldsField.setResponder(search -> this.worldList.setSearch(search));
		this.searchButton = Button.builder(Component.translatable("datapackdownloader.button.search"), button -> this.datapackList.updateDatapacks(this.fetchProjects(0), true)).bounds(120 + 12 + 28 + 5 + (this.width - (28 * 2) - (120 + 12 + 5)) - 50, 36, 50, 24).build();
		this.moreButton = Button.builder(Component.translatable("datapackdownloader.button.results"), button -> this.datapackList.updateDatapacks(this.fetchProjects(100*this.datapackList.moreIndex), false)).bounds(28, height - 30, 100, 24).build();
		this.backButton = Button.builder(Component.translatable("datapackdownloader.button.back"), button -> this.onClose()).bounds(width - 60, height -28, 50, 20).build();
		this.worldList = new DatapackWorldListWidget(this, this.minecraft, tempPath);
		this.worldList.setX(28);
		this.datapackList = new DatapackListWidget(this, this.minecraft);
		this.datapackList.setX(28 + 120 + 12 + 5);
		this.datapackInfoList = new DatapackInfoListWidget(this, this.minecraft);
		this.datapackInfoList.setX(width/2 + 160);
		this.addWidget(this.searchDatapacksField);
		this.addWidget(this.searchWorldsField);
		this.addWidget(this.searchButton);
		this.addWidget(this.worldList);
		this.addWidget(this.datapackList);
		this.addWidget(this.datapackInfoList);
		this.addWidget(this.moreButton);
		this.addWidget(this.backButton);
	}

	public ResultInfo fetchProjects(int offset) {
        URI uri;
		try {
			String version = FabricLoader.getInstance().getModContainer("minecraft").orElseThrow(() -> new RuntimeException("Failed to get minecraft mod info?")).getMetadata().getVersion().getFriendlyString();
			String encodedQuery = URLEncoder.encode(this.searchDatapacksField.getValue(), StandardCharsets.UTF_8);
			uri = new URI("https://api.modrinth.com/v2/search?query=" + encodedQuery + "&limit=100&offset=" + offset +"&facets=%5B%5B%22categories%3Adatapack%22%5D%2C%5B%22versions%3A" + version + "%22%5D%5D");
		} catch (URISyntaxException e) {
			Mod.LOGGER.error(e.getMessage());
			return new ResultInfo(List.of());
		}
		HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
		HttpClient client = HttpClient.newHttpClient();
		HttpResponse<String> response;
		try {
			response = client.send(request, HttpResponse.BodyHandlers.ofString());
		} catch (IOException | InterruptedException e) {
			return new ResultInfo(List.of());
		}
		return new Gson().fromJson(response.body(), ResultInfo.class);
	}
	
	@Override
	public void extractRenderState(@NonNull GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
		super.extractRenderState(context, mouseX, mouseY, delta);

		context.centeredText(this.font, this.title, this.width / 2, 15, 0xFFFFFFFF);
		context.text(this.font, Component.translatable("datapackdownloader.label.search.datapacks"), 120 + 12 + 28 + 5, 26, 0xFFA0A0A0);
		context.text(this.font, Component.translatable("datapackdownloader.label.results", this.totalResult), width / 2, height - 25, 0xFFA0A0A0);
		
		if (tempPath.isEmpty()) {
			this.searchWorldsField.extractRenderState(context, mouseX, mouseY, delta);
			context.text(this.font, Component.translatable("datapackdownloader.label.search.worlds"), 28, 26, 0xFFA0A0A0);
			this.worldList.extractRenderState(context, mouseX, mouseY, delta);
		} else {
			for(int i = 0; i < minecraft.font.split(Component.translatable("datapackdownloader.download.warning"), 132 - 10).size(); i++) {
				context.text(this.minecraft.font, minecraft.font.split(Component.translatable("datapackdownloader.download.warning"), 132 - 10).get(i), 28, 30 + 10 * i, 0xFFFFFFFF);
			}
		}

		this.datapackList.extractRenderState(context, mouseX, mouseY, delta);
		if (this.width >= 800) {
			this.datapackInfoList.extractWidgetRenderState(context, mouseX, mouseY, delta);
		} else {
			this.datapackList.setWidth(this.width - 200);
		}
		this.searchButton.extractRenderState(context, mouseX, mouseY, delta);
		this.moreButton.extractRenderState(context, mouseX, mouseY, delta);
		this.backButton.extractRenderState(context, mouseX, mouseY, delta);
		this.searchDatapacksField.extractRenderState(context, mouseX, mouseY, delta);

		if (!Objects.equals(this.oldSelectedWorld, this.worldList.getSelectedName())) {
			this.oldSelectedWorld = this.worldList.getSelectedName();
			this.datapackList.updateDatapacks(fetchProjects(0), true);
		}
		if (this.datapackList.getSelected() != null) {
			this.datapackInfoList.updateDatapack();
		}
	}

	@Override
	public boolean keyPressed(KeyEvent keyInput) {
		if (keyInput.key() == GLFW.GLFW_KEY_ESCAPE) {
			this.minecraft.setScreen(this.parent);
			return true;
		}
		return super.keyPressed(keyInput);
	}
	
	public void installDatapack(String slug, String latest_version) {

        URI uri;
		try {
			String version = FabricLoader.getInstance().getModContainer("minecraft").orElseThrow(() -> new RuntimeException("Failed to get minecraft mod info?")).getMetadata().getVersion().getFriendlyString();
			uri = new URI("https://api.modrinth.com/v2/project/" + slug + "/version?game_versions=%5B%22" + version + "%22%5D" + "&loaders=%5B%22datapack%22%5D");
		} catch (URISyntaxException e) {
			Mod.LOGGER.error(e.getMessage());
			return;
		}
		HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
		HttpClient client = HttpClient.newHttpClient();
		HttpResponse<String> response;
		try {
			response = client.send(request, HttpResponse.BodyHandlers.ofString());
		} catch (IOException | InterruptedException e) {
			return;
		}
		List<VersionInfo> versions = Arrays.stream(new Gson().fromJson(response.body(), VersionInfo[].class)).toList();
		if (versions.isEmpty() ) {
			Mod.LOGGER.error("Something went wrong!");
			return;
		}
		VersionInfo version = versions.getFirst();

		for (FileInfo file : version.files) {
			String url = file.url;
			try {
				FileUtils.copyURLToFile(new URI(url).toURL(), new File(getDatapackPath(slug)));
			} catch (IOException | URISyntaxException e) {
				e.printStackTrace();
			}
			writeJson(slug, latest_version);
		}
	}

	public boolean uninstallDatapack(String slug) {
		try {
			Files.deleteIfExists(new File(this.getDatapackPath(slug)).toPath());
			deleteJson(slug);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void updateDatapack(String slug, String latest_version) {
		uninstallDatapack(slug);
		installDatapack(slug, latest_version);
	}

	public void readJson() {
		try {
			FileReader file = new FileReader(getWorldPath() + "datapackdownloader.json");
			this.datapackDownloaderData = JsonParser.parseReader(file).getAsJsonObject();
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void writeJson(String slug, String version) {
		this.datapackDownloaderData.addProperty(slug, version);
		try {
			FileWriter file = new FileWriter(getWorldPath() + "datapackdownloader.json");
			file.write(String.valueOf(this.datapackDownloaderData));
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void deleteJson(String slug) {
		this.datapackDownloaderData.remove(slug);
		try {
			FileWriter file = new FileWriter(getWorldPath() + "datapackdownloader.json");
			file.write(String.valueOf(this.datapackDownloaderData));
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isOutdated(String slug, String version) {
		if (this.datapackDownloaderData == null || this.datapackDownloaderData.get(slug) == null) {
			return false;
		}
		return !this.datapackDownloaderData.get(slug).getAsString().equals(version);
	}
	
	protected String getDatapackPath(String slug) {
		if (this.worldList.getSelected() == null && tempPath.isEmpty()) {
			return "";
		}

		if (!tempPath.isEmpty()) {
			return tempPath + "/" + slug + ".zip";
		}

		return this.getWorldPath() + slug + ".zip";
	}

	protected String getWorldPath() {

        if (this.worldList.getSelected() == null) {
			return "";
		}

		if (this.worldList.getSelectedName().equals("Global Datapack (mod)")) {
			return FabricLoader.getInstance().getGameDir().resolve("datapacks") + "/";
		}

		return this.minecraft.getLevelSource().getBaseDir().toAbsolutePath() + "/" + this.worldList.getSelectedName() + "/datapacks/";
	}
}
