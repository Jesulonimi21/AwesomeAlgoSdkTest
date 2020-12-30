import { NativeModules } from 'react-native';
const { AlgoModule } = NativeModules;
interface AlgoInterface{
    createNewAccount():void
}
export default AlgoModule;
