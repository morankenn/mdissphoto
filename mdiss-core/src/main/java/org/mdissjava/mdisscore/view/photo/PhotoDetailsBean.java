package org.mdissjava.mdisscore.view.photo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.mdissjava.commonutils.properties.PropertiesFacade;
import org.mdissjava.commonutils.utils.Utils;
import org.mdissjava.mdisscore.controller.api.third.TwitterApiManager;
import org.mdissjava.mdisscore.controller.bll.impl.AlbumManagerImpl;
import org.mdissjava.mdisscore.controller.bll.impl.PhotoManagerImpl;
import org.mdissjava.mdisscore.metadata.impl.MetadataExtractorImpl;
import org.mdissjava.mdisscore.model.dao.factory.MorphiaDatastoreFactory;
import org.mdissjava.mdisscore.model.pojo.Album;
import org.mdissjava.mdisscore.model.pojo.OauthAccessToken;
import org.mdissjava.mdisscore.model.pojo.Photo;
import org.mdissjava.mdisscore.view.params.ParamsBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import twitter4j.TwitterException;

import com.google.code.morphia.Datastore;
import com.ocpsoft.pretty.PrettyContext;
import com.ocpsoft.pretty.faces.config.mapping.UrlMapping;
import com.ocpsoft.pretty.faces.util.PrettyURLBuilder;

@ViewScoped
@ManagedBean
public class PhotoDetailsBean {
	
	private String photoId;
	private String userNick;
	private String loggedUserNick;
	
	private List<String> defaultPhotoSizes;
	private List<String> thumbnailIds;
	private String thumbnailBucket;
	private String thumbnailDatabase;
	
	private String detailedPhotoURL;
	
	private List<String> metadataKeys;
	
	private Photo photo;
	private int likes;
	private int dislikes;
	
	private String tweetMessage;
	private String executeModal;
	private String publicLink;
	
	private final String GLOBAL_PROPS_KEY = "globals";
	private final String MORPHIA_DATABASE_KEY = "morphia.db";
	private final String RESOLUTIONS_PROPS_KEY = "resolutions";
	private final int PHOTO_SHOW_SIZE = 640;
	private Map<String, String> metadataMap;
	private String informationMessage = "";

	private String selectedAlbum; 
	private List<String> albumTitles;
	private int totalVotesPoints;
	private String myTags;
	private PhotoManagerImpl photoManager;
	private AlbumManagerImpl albumManager;
	private List<Photo> photosFromTag;
	private List<Album> albumList;
	private Album album;
	private String misTags;
	
	private String varAux;
	
	public PhotoDetailsBean() {
		ParamsBean pb = getPrettyfacesParams();
		this.userNick = pb.getUserId();
		this.photoId = pb.getPhotoId();
		this.loggedUserNick = this.retrieveSessionUserNick();
		
		//TODO: check if isn't detailed to redirect to /user/xxx/upload/details/yyy-yyyyyy-yyyy-yyy
		
		try {
			//get morphia database from properties and load the photo by its id
			String database;
			PropertiesFacade propertiesFacade = new PropertiesFacade();
			database = propertiesFacade.getProperties(GLOBAL_PROPS_KEY).getProperty(MORPHIA_DATABASE_KEY);
		
			Datastore datastore = MorphiaDatastoreFactory.getDatastore(database);
			photoManager = new PhotoManagerImpl(datastore);
			albumManager = new AlbumManagerImpl(datastore);
			
			this.photo = photoManager.searchPhotoUniqueUtil(photoId);
			
			//set the public link and the default message
			HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
			//String host = request.getServerName();
			//int port = request.getServerPort();
			//String app = request.getContextPath();
			//System.out.println(host + String.valueOf(port) + app);
			String url = Utils.getCurrentUrl(request);
			this.varAux=url;
			this.publicLink = url + this.getPublicPrettyURL(this.photo.getPhotoId(), this.photo.getPublicToken());
			this.tweetMessage = "Check out: "+this.publicLink+" @mdissphoto";
			
			//get metadata in a visible format
			this.metadataMap = new MetadataExtractorImpl().getMetadataFormatted(this.photo.getMetadata());
			System.out.println(metadataMap);
			metadataKeys = new ArrayList<String>();
			for(String key: metadataMap.keySet()){
				metadataKeys.add(key);
			}

			
			//search the available sizes for this photo
			//int sizes[] = {100, 240, 320, 500, 640, 800, 1024}; //our different sizes
			Properties allResolutions = propertiesFacade.getProperties(RESOLUTIONS_PROPS_KEY);
			
			this.defaultPhotoSizes = new ArrayList<String>();
			//set the size
			int height = this.photo.getMetadata().getResolutionREAL().getHeight();
			int width = this.photo.getMetadata().getResolutionREAL().getWidth();
			int photoSize = height > width ? height: width;
			
			// we get all the available resolutions
			@SuppressWarnings("rawtypes")
			Enumeration resolutions = allResolutions.keys();
			
			String key;
			//for each one we check if is scalar one and not square and if is smaller than the photo
			while(resolutions.hasMoreElements())
			{
				key = (String)resolutions.nextElement();
				
				//Only needed the scale ones, not the squares
				if (allResolutions.getProperty(key).contains("scale"))
				{
					//if is bigger than our photo size then don't add to the list of available sizes for this photo
					if (photoSize >= Integer.valueOf(key))
					{
						this.defaultPhotoSizes.add(key);
					}
				}
					
			}
			
			// we want to know which is the best photo for the display of the detail. 
			// Max is 640px but some photos are smaller than 640px so we set the original size
			//and if the photo is bigger than 640 then set the size to 640
			//get the database of the photos and create the url with the appropiate image
			
			String bucket;
			String bucketPropertyKey = null;
			if(photoSize > PHOTO_SHOW_SIZE)//640px size
			{
				bucketPropertyKey = "thumbnail.scale." + PHOTO_SHOW_SIZE + "px.bucket.name";
				bucket = propertiesFacade.getProperties("thumbnails").getProperty(bucketPropertyKey);
			}
			else//original size
			{
				bucketPropertyKey = "images.bucket";
				bucket = propertiesFacade.getProperties("globals").getProperty(bucketPropertyKey);
			}
			
			database = propertiesFacade.getProperties("globals").getProperty("images.db");
			this.detailedPhotoURL = "/dynamic/image?db="+database+"&amp;bucket="+bucket+"&amp;id="+this.photoId;
			
			
			//set the album thumbnails identifiers and necessary data
			this.thumbnailIds= new ArrayList<String>();
			album = this.photo.getAlbum();
			this.thumbnailBucket = "square.75";
			this.thumbnailDatabase = database;
			
			for (Photo i: album.getPhotos())
			{
				if(!i.getPhotoId().equals(this.photoId))
					this.thumbnailIds.add(i.getPhotoId());
			}
			
			//get the album from the photo
			String albumTitle = this.photo.getAlbum().getTitle();
			//get all the albums from the userNick
			this.albumList = albumManager.findUserAlbums(this.userNick);			
			this.albumTitles = new ArrayList<String>();
			List<String> otherAlbumTitles = new ArrayList<String>();
			for (Album a: albumList)
			{	
				//insert the first the album of that photo
				if(a.getTitle().equals(albumTitle)){
					this.albumTitles.add(a.getTitle());
				}
				else {
					otherAlbumTitles.add(a.getTitle());
				}
			}
			//add the others map at the end of the list
			this.albumTitles.addAll(otherAlbumTitles);
			
			// get total votes from the photoId
			this.totalVotesPoints = photoManager.getTotalVotesFromPhoto(this.photoId);
			
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		DisqusJsonReader djr=new DisqusJsonReader();
		try {
			this.varAux+="/u/"+this.getPrettyfacesParams().getUserId()+"/photo/"+this.getPrettyfacesParams().getPhotoId();
			ArrayList<Integer> arrayAux=djr.readLikesAndDislikes(this.varAux);
			this.setLikes(arrayAux.get(0));
			this.setDislikes(arrayAux.get(1));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}

	public void startTweeterBirdOauthAuthProcess() throws TwitterException, IOException{
		//check if we have the credentials id not redirect
		OauthAccessToken accessToken = null;
		try
		{
			accessToken = new TwitterApiManager().getUserOauthCredentials(loggedUserNick);
		}catch(Exception e)
		{
			//if there was an illegal access then we need to create the user, so we redirect to the twitter oauth page
			//so we don't do anything because will enter in the null block
		}
		
		if (accessToken == null)
		{	
			TwitterApiManager twitterApi = new TwitterApiManager();
			String url = twitterApi.getTwitterTokenUrl(this.loggedUserNick);
			ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			externalContext.redirect(url);
		}else
		{
			this.executeModal = "$('#myModal').modal('show')";
		}
	}
	
	public void tweetStatus() throws TwitterException{
		try{
			OauthAccessToken accessToken = new TwitterApiManager().getUserOauthCredentials(loggedUserNick);
			new TwitterApiManager().updatestatus(accessToken, this.tweetMessage);
			//hide the modal
			this.executeModal = "";
			this.informationMessage  = "<div class=\"alert alert-success\">" +
					"<button class=\"close\" data-dismiss=\"alert\">×</button>" +
					" Tweeted succesfully :)</div>";
		}catch (Exception e){
			System.out.println(e.toString());
			this.informationMessage  = "<div class=\"alert alert-error\">" +
						"<button class=\"close\" data-dismiss=\"alert\">×</button>" +
					" There was an error eith the tweet. Try again please </div>";
		}
	} 
	
	public String getPhotoId() {
		return photoId;
	}

	public void setPhotoId(String photoId) {
		this.photoId = photoId;
	}

	public String getUserNick() {
		return userNick;
	}

	public void setUserNick(String userNick) {
		this.userNick = userNick;
	}
	
	public List<String> getDefaultPhotoSizes() {
		return defaultPhotoSizes;
	}

	public void setDefaultPhotoSizes(List<String> defaultPhotoSizes) {
		this.defaultPhotoSizes = defaultPhotoSizes;
	}
	
	public String getThumbnailDatabase() {
		return thumbnailDatabase;
	}

	public void setThumbnailDatabase(String thumbnailDatabase) {
		this.thumbnailDatabase = thumbnailDatabase;
	}

	public List<String> getThumbnailIds() {
		return thumbnailIds;
	}

	public void setThumbnailIds(List<String> thumbnailIds) {
		this.thumbnailIds = thumbnailIds;
	}

	public String getThumbnailBucket() {
		return thumbnailBucket;
	}

	public void setThumbnailBucket(String thumbnailBucket) {
		this.thumbnailBucket = thumbnailBucket;
	}

	public String getDetailedPhotoURL() {
		return detailedPhotoURL;
	}

	public void setDetailedPhotoURL(String detailedPhotoURL) {
		this.detailedPhotoURL = detailedPhotoURL;
	}

	public Photo getPhoto() {
		return photo;
	}

	public void setPhoto(Photo photo) {
		this.photo = photo;
	}
	
	public List<String> getMetadataKeys() {
		return metadataKeys;
	}


	public void setMetadataKeys(List<String> metadataKeys) {
		this.metadataKeys = metadataKeys;
	}

	public Map<String, String> getMetadataMap() {
		return metadataMap;
	}


	public void setMetadataMap(Map<String, String> metadataMap) {
		this.metadataMap = metadataMap;
	}

	public String getLoggedUserNick() {
		return loggedUserNick;
	}

	public void setLoggedUserNick(String loggedUserNick) {
		this.loggedUserNick = loggedUserNick;
	}

	public String getTweetMessage() {
		return tweetMessage;
	}

	public void setTweetMessage(String tweetMessage) {
		this.tweetMessage = tweetMessage;
	}

	
	public String getInformationMessage() {
		return informationMessage;
	}

	public void setInformationMessage(String informationMessage) {
		this.informationMessage = informationMessage;
	}

	public String getExecuteModal() {
		return executeModal;
	}

	public void setExecuteModal(String executeModal) {
		this.executeModal = executeModal;
	}

	
	public String getPublicLink() {
		return publicLink;
	}

	public void setPublicLink(String publicLink) {
		this.publicLink = publicLink;
	}

	public List<String> getAlbumTitles() {
		return this.albumTitles;
	}
	
	public void setAlbumTitles(List<String> albumTitles) {
		this.albumTitles = albumTitles;
	}
	
	public String getSelectedAlbum() { 
		return selectedAlbum;
	}
	
	public void setSelectedAlbum(String selectedAlbum) {
		this.selectedAlbum = selectedAlbum;
	}

	public int getTotalVotesPoints() {
		return totalVotesPoints;
	}
	
	public void setTotalVotesPoints(int totalVotesPoints) {
		this.totalVotesPoints = totalVotesPoints;
	}

	public String getMyTags() {
		List<String> tags = this.photo.getTags();
		myTags = tags.toString();
		return myTags;
	}

	public void setMyTags(String myTags) {
		this.myTags = myTags;
	}
	
	public List<Photo> getPhotosFromTag() {
		return photosFromTag;
	}

	public void setPhotosFromTag(List<Photo> photosFromTag) {
		this.photosFromTag = photosFromTag;
	}

	public List<Album> getAlbumList() {
		return albumList;
	}
	
	public void setAlbumList(List<Album> albumList) {
		this.albumList = albumList;
	}
	
	public String getMisTags() {
		return misTags;
	}

	public void setMisTags(String misTags) {
		System.out.println("Mistags: " + misTags);
		this.misTags = misTags;
	}
	
	public int getLikes() {
		return likes;
	}

	public void setLikes(int likes) {
		this.likes = likes;
	}

	public int getDislikes() {
		return dislikes;
	}

	public void setDislikes(int dislikes) {
		this.dislikes = dislikes;
	}
	
	public float getMark() {
		if(dislikes>0 || likes>0)
			return (likes/(dislikes+likes))*10;
		else
			return 5;
	}

	private ParamsBean getPrettyfacesParams()
	{
		FacesContext context = FacesContext.getCurrentInstance();
		ParamsBean pb = (ParamsBean) context.getApplication().evaluateExpressionGet(context, "#{paramsBean}", ParamsBean.class);
		return pb;
	}
	
	private String retrieveSessionUserNick() {
		//Get the current logged user's username
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		return auth.getName();
		
	}
	
	private String getPublicPrettyURL(String photoParam, String tokenParam)
	{
		String mappingUrl = "public-photo";
		HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
		PrettyContext context = PrettyContext.getCurrentInstance(request);
		PrettyURLBuilder builder = new PrettyURLBuilder();
		
		UrlMapping mapping = context.getConfig().getMappingById(mappingUrl);
		Object[] objs = {photoParam, tokenParam};
		return builder.build(mapping, true, objs);
		
		
	}
	
	public void saveSettings()
	{
		System.out.println("saveSettings");
		String allTags = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("misTags");
		System.out.println("allTags: " + allTags);		
		System.out.println("title: " + photo.getTitle());
		System.out.println("PhotoDetailsBean.doSave()");
		Album readNewAlbum = null;
		
		//add the album to database
			if(!this.photo.getTitle().equals("")){
		
				//get the photo
				this.photo.setTitle(this.photo.getTitle());
									
				//get the tags
				System.out.println("Tags photo: " + this.photo.getTags());
				if (!allTags.equals(""))
				{ 
					//get the list of tags from the photo
					List<String> newTagList = Utils.splitTags(allTags, ",");
					// add the new tags to the prevous list stored in db
					System.out.println("tags: " + newTagList.toString());
					this.photo.setTags(newTagList);						
				}			
				//update modified photo
				this.photoManager.updatePhoto(this.photo);

				
				//get the new object album from title selected
				Album newAlbum = new Album();
				newAlbum.setTitle(this.selectedAlbum);
				List<Album> readListAlbum = albumManager.findAlbum(newAlbum);
				//add the new photo to the album has found previously 
				if(readListAlbum != null) {
					readNewAlbum = readListAlbum.get(0);
					// check if newAlbum is the same, if there are not the same move the album
					if(!readNewAlbum.getAlbumId().equals(this.photo.getAlbum().getAlbumId())) {
						try {
							albumManager.movePhotoToAlbum(this.userNick, readNewAlbum.getAlbumId(), this.photo);
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}						
						
					}
					
				}
				
				//Navigation to photo-detail view
				String outcome = "pretty:photo-detail";
				FacesContext facesContext =  FacesContext.getCurrentInstance();
				facesContext.getApplication().getNavigationHandler().handleNavigation(facesContext, null, outcome);
			}
			
	}

	public String getVarAux() {
		return varAux;
	}

	public void setVarAux(String varAux) {
		this.varAux = varAux;
	}


	
}