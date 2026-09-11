package com.alessio89g.mysearchwidget.icons
import androidx.compose.material.icons.Icons
import com.alessio89g.mysearchwidget.icons.filled.*
import com.alessio89g.mysearchwidget.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

data class IconEntry(val name:String,val words:String,val filled:ImageVector,val outlined:ImageVector)
object IconCatalog {
 // Curated offline subset of Material Icons Extended, cached once per app process.
 val entries:List<IconEntry> by lazy { listOf(
  IconEntry("Search","cerca ricerca lente",Icons.Filled.Search,Icons.Outlined.Search),
  IconEntry("Mic","microfono voce",Icons.Filled.Mic,Icons.Outlined.Mic),
  IconEntry("CameraAlt","fotocamera foto",Icons.Filled.CameraAlt,Icons.Outlined.CameraAlt),
  IconEntry("Translate","traduci lingue",Icons.Filled.Translate,Icons.Outlined.Translate),
  IconEntry("WbSunny","meteo sole",Icons.Filled.WbSunny,Icons.Outlined.WbSunny),
  IconEntry("MusicNote","musica brano",Icons.Filled.MusicNote,Icons.Outlined.MusicNote),
  IconEntry("AutoAwesome","intelligenza AI",Icons.Filled.AutoAwesome,Icons.Outlined.AutoAwesome),
  IconEntry("Home","casa home",Icons.Filled.Home,Icons.Outlined.Home),
  IconEntry("Mail","posta email",Icons.Filled.Mail,Icons.Outlined.Mail),
  IconEntry("Phone","telefono",Icons.Filled.Phone,Icons.Outlined.Phone),
  IconEntry("Chat","messaggi chat",Icons.Filled.Chat,Icons.Outlined.Chat),
  IconEntry("Settings","impostazioni",Icons.Filled.Settings,Icons.Outlined.Settings),
  IconEntry("Person","persona contatti",Icons.Filled.Person,Icons.Outlined.Person),
  IconEntry("Star","stella preferiti",Icons.Filled.Star,Icons.Outlined.Star),
  IconEntry("Favorite","cuore preferiti",Icons.Filled.Favorite,Icons.Outlined.Favorite),
  IconEntry("Map","mappa",Icons.Filled.Map,Icons.Outlined.Map),
  IconEntry("Place","luogo posizione",Icons.Filled.Place,Icons.Outlined.Place),
  IconEntry("Public","web browser mondo",Icons.Filled.Public,Icons.Outlined.Public),
  IconEntry("ShoppingCart","carrello acquisti",Icons.Filled.ShoppingCart,Icons.Outlined.ShoppingCart),
  IconEntry("CalendarMonth","calendario data",Icons.Filled.CalendarMonth,Icons.Outlined.CalendarMonth),
  IconEntry("AccessTime","ora orologio",Icons.Filled.AccessTime,Icons.Outlined.AccessTime),
  IconEntry("Alarm","sveglia",Icons.Filled.Alarm,Icons.Outlined.Alarm),
  IconEntry("Folder","cartella file",Icons.Filled.Folder,Icons.Outlined.Folder),
  IconEntry("Description","documento",Icons.Filled.Description,Icons.Outlined.Description),
  IconEntry("Photo","immagine galleria",Icons.Filled.Photo,Icons.Outlined.Photo),
  IconEntry("PlayArrow","riproduci play",Icons.Filled.PlayArrow,Icons.Outlined.PlayArrow),
  IconEntry("Pause","pausa",Icons.Filled.Pause,Icons.Outlined.Pause),
  IconEntry("Headphones","cuffie audio",Icons.Filled.Headphones,Icons.Outlined.Headphones),
  IconEntry("Bluetooth","bluetooth",Icons.Filled.Bluetooth,Icons.Outlined.Bluetooth),
  IconEntry("Wifi","wifi rete",Icons.Filled.Wifi,Icons.Outlined.Wifi),
  IconEntry("Computer","computer pc",Icons.Filled.Computer,Icons.Outlined.Computer),
  IconEntry("Smartphone","smartphone telefono",Icons.Filled.Smartphone,Icons.Outlined.Smartphone),
  IconEntry("DirectionsCar","auto macchina",Icons.Filled.DirectionsCar,Icons.Outlined.DirectionsCar),
  IconEntry("DirectionsWalk","cammina passeggiata",Icons.Filled.DirectionsWalk,Icons.Outlined.DirectionsWalk),
  IconEntry("Flight","volo aereo",Icons.Filled.Flight,Icons.Outlined.Flight),
  IconEntry("Train","treno",Icons.Filled.Train,Icons.Outlined.Train),
  IconEntry("Restaurant","ristorante cibo",Icons.Filled.Restaurant,Icons.Outlined.Restaurant),
  IconEntry("LocalCafe","caffe bar",Icons.Filled.LocalCafe,Icons.Outlined.LocalCafe),
  IconEntry("FitnessCenter","palestra fitness",Icons.Filled.FitnessCenter,Icons.Outlined.FitnessCenter),
  IconEntry("Pets","animali",Icons.Filled.Pets,Icons.Outlined.Pets),
  IconEntry("Lightbulb","lampadina luce",Icons.Filled.Lightbulb,Icons.Outlined.Lightbulb),
  IconEntry("Lock","lucchetto",Icons.Filled.Lock,Icons.Outlined.Lock),
  IconEntry("Shield","scudo sicurezza",Icons.Filled.Shield,Icons.Outlined.Shield),
  IconEntry("Cloud","nuvola cloud",Icons.Filled.Cloud,Icons.Outlined.Cloud),
  IconEntry("Download","scarica download",Icons.Filled.Download,Icons.Outlined.Download),
  IconEntry("Upload","carica upload",Icons.Filled.Upload,Icons.Outlined.Upload),
  IconEntry("Videocam","video",Icons.Filled.Videocam,Icons.Outlined.Videocam),
  IconEntry("QrCodeScanner","qr scanner",Icons.Filled.QrCodeScanner,Icons.Outlined.QrCodeScanner),
  IconEntry("Lens","lens cerchio",Icons.Filled.Lens,Icons.Outlined.Lens),
  IconEntry("MoreHoriz","altro punti",Icons.Filled.MoreHoriz,Icons.Outlined.MoreHoriz)
 ) }
 fun vector(name:String,outline:Boolean):ImageVector { val e=entries.firstOrNull { it.name==name } ?: entries.first();return if(outline)e.outlined else e.filled }
}
