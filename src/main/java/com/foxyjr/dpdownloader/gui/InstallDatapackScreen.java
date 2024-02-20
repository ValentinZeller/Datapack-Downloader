package com.foxyjr.dpdownloader.gui;

import com.google.gson.Gson;
import com.foxyjr.dpdownloader.Mod;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.apache.commons.io.FileUtils;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class InstallDatapackScreen extends Screen {
	public int totalResult = 0;
	private TextFieldWidget searchDatapacksField;
	private TextFieldWidget searchWorldsField;
	private ButtonWidget searchButton;
	private ButtonWidget moreButton;
	private final Screen parent;
	public DatapackWorldListWidget worldList;
	public DatapackListWidget datapackList;
	public DatapackInfoListWidget datapackInfoList;
	private String oldSelectedWorld;
	
	public InstallDatapackScreen(Screen parent) {
		super(Text.of("Install Datapacks"));
		this.parent = parent;
	}
	
	@Override
	public void resize(MinecraftClient client, int width, int height) {
		String oldSearchDatapacksField = this.searchDatapacksField.getText();
		String oldSearchWorldsField = this.searchWorldsField.getText();
		List<DatapackInfo> oldDatapackInfo = this.datapackList.getDatapacks();
		this.init(client, width, height);
		this.searchDatapacksField.setText(oldSearchDatapacksField);
		this.searchWorldsField.setText(oldSearchWorldsField);
		this.datapackList.setDatapacks(oldDatapackInfo);
	}
	
	@Override
	protected void init() {
		this.searchDatapacksField = new TextFieldWidget(this.textRenderer, 120 + 12 + 28 + 5, 38, this.width - (28 * 2) - (120 + 12 + 5) - 56, 20, Text.of("Search datapacks"));
		this.searchWorldsField = new TextFieldWidget(this.textRenderer, 28, 38, 132, 20, Text.of("Search worlds"));
		this.searchWorldsField.setChangedListener(search -> this.worldList.setSearch(search));
		this.searchButton = ButtonWidget.builder(Text.of("Search"), button -> this.datapackList.updateDatapacks(this.fetchProjects(0), true)).dimensions(120 + 12 + 28 + 5 + (this.width - (28 * 2) - (120 + 12 + 5)) - 50, 36, 50, 24).build();
		this.moreButton = ButtonWidget.builder(Text.of("More results"), button -> this.datapackList.updateDatapacks(this.fetchProjects(100*this.datapackList.moreIndex), false)).dimensions(28, height - 30, 100, 24).build();
		this.worldList = new DatapackWorldListWidget(this, this.client);
		this.worldList.setX(28);
		this.datapackList = new DatapackListWidget(this, this.client);
		this.datapackList.setX(28 + 120 + 12 + 5);
		this.datapackInfoList = new DatapackInfoListWidget(this, this.client);
		this.datapackInfoList.setX(width/2 + 160);
		this.addSelectableChild(this.searchDatapacksField);
		this.addSelectableChild(this.searchWorldsField);
		this.addSelectableChild(this.searchButton);
		this.addSelectableChild(this.worldList);
		this.addSelectableChild(this.datapackList);
		this.addSelectableChild(this.datapackInfoList);
		this.addSelectableChild(this.moreButton);
	}

	public ResultInfo fetchProjects(int offset) {
		if (this.client == null) {
			return new ResultInfo(List.of());
		}
		URI uri;
		try {
			String version = FabricLoader.getInstance().getModContainer("minecraft").orElseThrow(() -> new RuntimeException("Failed to get minecraft mod info?")).getMetadata().getVersion().getFriendlyString();
			String encodedQuery = URLEncoder.encode(this.searchDatapacksField.getText(), StandardCharsets.UTF_8);
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
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		this.renderBackground(context, mouseX, mouseY, delta);
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
		context.drawTextWithShadow(this.textRenderer, "Search for worlds", 28, 26, 0xA0A0A0);
		context.drawTextWithShadow(this.textRenderer, "Search for datapacks", 120 + 12 + 28 + 5, 26, 0xA0A0A0);
		context.drawTextWithShadow(this.textRenderer, "Results : "+this.totalResult, width / 2, height - 25, 0xA0A0A0);
		this.worldList.render(context, mouseX, mouseY, delta);
		this.datapackList.render(context, mouseX, mouseY, delta);
		if (this.width >= 720) {
			this.datapackInfoList.render(context, mouseX, mouseY, delta);
		} else {
			this.datapackList.setWidth(this.width - 200);
		}
		this.searchButton.render(context, mouseX, mouseY, delta);
		this.moreButton.render(context, mouseX, mouseY, delta);
		this.searchDatapacksField.render(context, mouseX, mouseY, delta);
		this.searchWorldsField.render(context, mouseX, mouseY, delta);
		if (!Objects.equals(this.oldSelectedWorld, this.worldList.getSelected())) {
			this.oldSelectedWorld = this.worldList.getSelected();
			this.datapackList.updateDatapacks(fetchProjects(0), true);
		}
		/*if (this.datapackInfoList.getSelectedOrNull() != null) {

		}*/
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (client != null && keyCode == GLFW.GLFW_KEY_ESCAPE) {
			this.client.setScreen(this.parent);
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	public void installDatapack(String slug) {
		if (this.client == null) {
			return;
		}
		
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
		if (versions.size() == 0) {
			Mod.LOGGER.error("Something went wrong!");
			return;
		}
		VersionInfo version = versions.get(0);
		for (FileInfo file : version.files) {
			String url = file.url;
			try {
				FileUtils.copyURLToFile(new URL(url), new File(getDatapackPath(slug)));
			} catch (IOException e) {
				return;
			}
		}
	}
	
	protected String getDatapackPath(String slug) {
		if (this.client == null) {
			return "";
		}
		
		return this.client.getLevelStorage().getSavesDirectory().toAbsolutePath() + "/" + this.worldList.getSelected() + "/datapacks/" + slug + ".zip";
	}
}
